package com.traverse.appliedthaumics.part;

import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import com.traverse.appliedthaumics.registry.ATItems;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.traverse.appliedthaumics.registry.ATMenus;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ArcaneTerminalPart extends AbstractTerminalPart implements IUpgradeableObject {
    public static final int GRID_SLOTS = 9;
    public static final int CRYSTAL_START = 9;
    public static final int CRYSTAL_SLOTS = 6;
    public static final int WAND_SLOT = 15;
    public static final int SIZE = 16;

    private final AppEngInternalInventory arcaneInventory = new AppEngInternalInventory(this, SIZE);
    private final IUpgradeInventory upgrades;
    private UUID hostId = UUID.randomUUID();

    public ArcaneTerminalPart(IPartItem<?> partItem) {
        super(partItem);
        this.upgrades = UpgradeInventories.forMachine(partItem, 1, this::onUpgradesChanged);
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
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ATMenus.ARCANE_TERMINAL;
    }
}
