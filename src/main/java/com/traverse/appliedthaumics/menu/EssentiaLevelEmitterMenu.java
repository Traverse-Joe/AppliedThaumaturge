package com.traverse.appliedthaumics.menu;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigMenuInventory;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.me.strategy.EssentiaContainerItemStrategy;
import com.traverse.appliedthaumics.part.EssentiaLevelEmitterPart;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EssentiaLevelEmitterMenu extends StorageLevelEmitterMenu {
    private static final ClientActionKey<Long> ACTION_SET_VALUE = new ClientActionKey<>("setReportingValue");
    private long currentValue;

    public EssentiaLevelEmitterMenu(MenuType<StorageLevelEmitterMenu> type, int id, Inventory inventory, EssentiaLevelEmitterPart host) {
        super(type, id, inventory, host);
    }

    public void initializeValue(long value) {
        currentValue = value;
    }

    @Override
    public long getCurrentValue() {
        return currentValue;
    }

    @Override
    public void setValue(long value) {
        if (isClientSide()) {
            if (value == currentValue) {
                return;
            }
            sendClientAction(ACTION_SET_VALUE, value);
        } else {
            getHost().setReportingValue(value);
        }
        currentValue = value;
    }

    @Override
    protected void setupUpgrades() {
    }

    @Override
    protected void setupConfig() {
        ConfigMenuInventory inventory = new ConfigMenuInventory(getHost().getConfig()) {
            private final EssentiaContainerItemStrategy strategy = new EssentiaContainerItemStrategy();

            @Nullable
            @Override
            public GenericStack convertToSuitableStack(ItemStack stack) {
                if (stack.isEmpty()) {
                    return null;
                }
                GenericStack wrapped = GenericStack.unwrapItemStack(stack);
                if (wrapped != null) {
                    if (wrapped.what() instanceof EssentiaKey) {
                        return wrapped;
                    }
                    if (!(wrapped.what() instanceof AEItemKey item)) {
                        return null;
                    }
                    stack = item.toStack();
                }
                if (stack.getItem() instanceof ItemEssentiaCrystal) {
                    EssentiaKey key = EssentiaKey.of(ItemEssentiaCrystal.aspectOf(stack));
                    return key == null ? null : new GenericStack(key, 1);
                }
                return strategy.getContainedStack(stack);
            }
        };
        addSlot(new FakeSlot(inventory, 0), SlotSemantics.CONFIG);
    }
}
