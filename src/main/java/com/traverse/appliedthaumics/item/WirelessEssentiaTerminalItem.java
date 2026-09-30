package com.traverse.appliedthaumics.item;

import appeng.api.util.KeyTypeSelection;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import java.util.function.DoubleSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WirelessEssentiaTerminalItem extends WirelessTerminalItem {
    public WirelessEssentiaTerminalItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Nullable
    @Override
    public WirelessTerminalMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new Host(this, player, locator);
    }

    private static final class Host extends WirelessTerminalMenuHost<WirelessEssentiaTerminalItem> {
        private final KeyTypeSelection essentiaOnly = new KeyTypeSelection(() -> {
        }, type -> type == EssentiaKeyType.TYPE);

        private Host(WirelessEssentiaTerminalItem item, Player player, ItemMenuHostLocator locator) {
            super(item, player, locator, (p, subMenu) -> item.openFromInventory(p, locator, true));
        }

        @Override
        public KeyTypeSelection getKeyTypeSelection() {
            return essentiaOnly;
        }
    }
}
