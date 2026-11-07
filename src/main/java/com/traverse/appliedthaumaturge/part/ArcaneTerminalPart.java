package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.resources.ResourceLocation;
import com.traverse.appliedthaumaturge.util.CodecTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import com.traverse.appliedthaumaturge.registry.ATItems;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import com.traverse.appliedthaumaturge.vis.VisRelayLink;
import com.traverse.appliedthaumaturge.vis.VisRelayCharging;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalHost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ArcaneTerminalPart extends AbstractTerminalPart implements IUpgradeableObject, IGridTickable, ArcaneTerminalHost {
    public static final int GRID_SLOTS = 9;
    public static final int CRYSTAL_START = 9;
    public static final int CRYSTAL_SLOTS = 6;
    public static final int WAND_SLOT = 15;
    public static final int SIZE = 16;

    @PartModels
    public static final ResourceLocation MODEL_OFF = AppliedThaumaturge.id("part/arcane_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppliedThaumaturge.id("part/arcane_terminal_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

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
    public ArcaneWorkbenchContext getArcaneContext(ServerPlayer player) {
        return ArcaneWorkbenchContext.placed(player, getAuraPosition(), hostId, null);
    }

    @Override
    public BlockPos getAuraPosition() {
        return getHost().getBlockEntity().getBlockPos();
    }

    @Override
    public void saveArcaneInventory() {
        getHost().markForSave();
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
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        arcaneInventory.readFromNBT(data, "arcaneGrid", registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        visSource = CodecTags.read(data, "visSource", VisRelayLink.CODEC, registries).orElse(null);
        if (data.contains("arcaneHostId")) {
            try {
                hostId = UUID.fromString(data.getString("arcaneHostId"));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        arcaneInventory.writeToNBT(data, "arcaneGrid", registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        data.putString("arcaneHostId", hostId.toString());
        if (visSource != null) {
            CodecTags.write(data, "visSource", VisRelayLink.CODEC, visSource, registries);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ATMenus.ARCANE_TERMINAL;
    }
}
