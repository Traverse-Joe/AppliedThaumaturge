package com.traverse.appliedthaumaturge.integration.wtlib.client;

import appeng.client.InitScreens;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class LibraryTerminalScreens {
    private LibraryTerminalScreens() {
    }

    public static void register(RegisterMenuScreensEvent event) {
        InitScreens.register(event, ATMenus.WIRELESS_ARCANE_TERMINAL, ArcaneWTScreen::new,
                "/screens/terminals/wireless_arcane_terminal.json");
        InitScreens.register(event, ATMenus.WIRELESS_ESSENTIA_TERMINAL, EssentiaWTScreen::new,
                "/screens/terminals/wireless_essentia_terminal.json");
    }
}
