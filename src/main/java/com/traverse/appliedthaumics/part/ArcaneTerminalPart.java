package com.traverse.appliedthaumics.part;

import appeng.api.parts.IPartItem;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import com.traverse.appliedthaumics.registry.ATItems;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.traverse.appliedthaumics.registry.ATMenus;
import com.traverse.appliedthaumics.vis.VisRelayLink;
import com.traverse.appliedthaumics.vis.VisRelayCharging;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ArcaneTerminalPart extends AbstractTerminalPart implements IUpgradeableObject, IGridTickable {
    public static final int GRID_SLOTS = 9;
    public static final int CRYSTAL_START = 9;
    public static final int CRYSTAL_SLOTS = 6;
    public static final int WAND_SLOT = 15;
    public static final int SIZE = 16;

    private final AppEngInternalInventory arcaneInventory = new AppEngInternalInventory(this, SIZE);
    private final IUpgradeInventory upgrades;
    private UUID hostId = UUID.randomUUID();
    private VisRelayLink visSource;

    public ArcaneTerminalPart(IPartItem<?> partItem) {
        super(partItem);
        this.upgrades = UpgradeInventories.forMachine(partItem, 1, this::onUpgradesChanged);
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public boolean onUseItemOn(ItemStack stack, Player player, InteractionHand hand, Vec3 hit) {
        if (stack.getItem() instanceof IMemoryCard card && !InteractionUtil.isInAlternateUseMode(player)) {
            VisRelayLink saved = VisRelayLink.fromCard(stack);
            boolean clear = visSource != null && !stack.has(AEComponents.EXPORTED_SETTINGS_SOURCE);
            if (saved != null || clear) {
                if (isClientSide()) {
                    return true;
                }
                if (!Platform.hasPermissions(getHost().getLocation(), player)) {
                    return false;
                }
                if (saved != null && saved.resolve(this) == null) {
                    card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                } else {
                    visSource = saved;
                    getHost().markForSave();
                    card.notifyUser(player, clear ? MemoryCardMessages.SETTINGS_CLEARED
                            : MemoryCardMessages.SETTINGS_LOADED);
                }
                return true;
            }
        }
        return super.onUseItemOn(stack, player, hand, hit);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 5, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        ItemStack wand = arcaneInventory.getStackInSlot(WAND_SLOT);
        VisRelayInterfacePart source = visSource == null ? null : visSource.resolve(this);
        if (VisRelayCharging.charge(wand, source)) {
            arcaneInventory.setItemDirect(WAND_SLOT, wand);
            getHost().markForSave();
        }
        return TickRateModulation.SAME;
    }

    private void onUpgradesChanged() {
        getHost().markForSave();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public boolean hasChargingCard() {
        return upgrades.isInstalled(ATItems.UPGRADE_ARCANE);
    }

    public AppEngInternalInventory getArcaneInventory() {
        return arcaneInventory;
    }

    public UUID getHostId() {
        return hostId;
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        for (ItemStack stack : arcaneInventory) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
        for (ItemStack stack : upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        arcaneInventory.clear();
        upgrades.clear();
    }

    @Override
    public void readFromNBT(ValueInput input) {
        super.readFromNBT(input);
        arcaneInventory.readFromNBT(input, "arcaneGrid");
        upgrades.readFromNBT(input, "upgrades");
        visSource = input.read("visSource", VisRelayLink.CODEC).orElse(null);
        input.getString("arcaneHostId").ifPresent(id -> {
            try {
                hostId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
        });
    }

    @Override
    public void writeToNBT(ValueOutput output) {
        super.writeToNBT(output);
        arcaneInventory.writeToNBT(output, "arcaneGrid");
        upgrades.writeToNBT(output, "upgrades");
        output.putString("arcaneHostId", hostId.toString());
        if (visSource != null) {
            output.store("visSource", VisRelayLink.CODEC, visSource);
        }
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ATMenus.ARCANE_TERMINAL;
    }
}
