package com.traverse.appliedthaumaturge.integration.wtlib;

import appeng.api.util.KeyTypeSelection;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.registry.ATTerminalHotkeys;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.player.Player;

public final class EssentiaWTMenuHost extends WTMenuHost {
    private final KeyTypeSelection essentiaOnly = new KeyTypeSelection(() -> {
    }, type -> type == EssentiaKeyType.TYPE);

    public EssentiaWTMenuHost(ItemWT item, Player player, ItemMenuHostLocator locator,
                             BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return essentiaOnly;
    }

    @Override
    public String getCloseHotkey() {
        return ATTerminalHotkeys.WIRELESS_ESSENTIA_TERMINAL;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !WirelessEssentiaTerminalItem.isAlchemyMode(getItemStack());
    }
}
