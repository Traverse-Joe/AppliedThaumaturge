package com.traverse.appliedthaumaturge.registry;

import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.api.stacks.GenericStack;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.assembler.ArcaneAssemblerBlockEntity;
import com.traverse.appliedthaumaturge.menu.ArcaneAssemblerMenu;
import com.traverse.appliedthaumaturge.menu.ArcaneInscriberMenu;
import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
import com.traverse.appliedthaumaturge.menu.EssentiaLevelEmitterMenu;
import com.traverse.appliedthaumaturge.menu.PortableEssentiaCellMenu;
import com.traverse.appliedthaumaturge.item.PortableEssentiaCellItem;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.part.ArcaneInscriberPart;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.part.EssentiaExportBusPart;
import com.traverse.appliedthaumaturge.part.EssentiaImportBusPart;
import com.traverse.appliedthaumaturge.part.EssentiaLevelEmitterPart;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ATMenus {
    public static final MenuType<PortableEssentiaCellMenu> PORTABLE_ESSENTIA_CELL = MenuTypeBuilder
            .create(PortableEssentiaCellMenu::new, PortableEssentiaCellItem.Host.class)
            .buildUnregistered(AppliedThaumaturge.id("portable_essentia_cell"));
    public static final MenuType<IOBusMenu> ESSENTIA_IMPORT_BUS = MenuTypeBuilder
            .create(IOBusMenu::new, EssentiaImportBusPart.class)
            .buildUnregistered(AppliedThaumaturge.id("essentia_import_bus"));
    public static final MenuType<IOBusMenu> ESSENTIA_EXPORT_BUS = MenuTypeBuilder
            .create(IOBusMenu::new, EssentiaExportBusPart.class)
            .buildUnregistered(AppliedThaumaturge.id("essentia_export_bus"));
    public static final MenuType<StorageLevelEmitterMenu> ESSENTIA_LEVEL_EMITTER = MenuTypeBuilder
            .create(EssentiaLevelEmitterMenu::new, EssentiaLevelEmitterPart.class)
            .withInitialData((host, buffer) -> {
                GenericStack.writeBuffer(host.getConfig().getStack(0), buffer);
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                host.getConfig().setStack(0, GenericStack.readBuffer(buffer));
                ((EssentiaLevelEmitterMenu) menu).initializeValue(buffer.readVarLong());
            })
            .buildUnregistered(AppliedThaumaturge.id("essentia_level_emitter"));

    public static final MenuType<ArcaneTermMenu> ARCANE_TERMINAL = MenuTypeBuilder
            .create(ArcaneTermMenu::new, ArcaneTerminalPart.class)
            .buildUnregistered(AppliedThaumaturge.id("arcane_terminal"));

    public static final MenuType<ArcaneTermMenu> WIRELESS_ARCANE_TERMINAL = MenuTypeBuilder
            .create(ArcaneTermMenu::new, WirelessArcaneTerminalItem.Host.class)
            .buildUnregistered(AppliedThaumaturge.id("wireless_arcane_terminal"));

    public static final MenuType<ArcaneInscriberMenu> ARCANE_INSCRIBER = MenuTypeBuilder
            .create(ArcaneInscriberMenu::new, ArcaneInscriberPart.class)
            .buildUnregistered(AppliedThaumaturge.id("arcane_inscriber"));

    public static final MenuType<ArcaneAssemblerMenu> ARCANE_ASSEMBLER = MenuTypeBuilder
            .create(ArcaneAssemblerMenu::new, ArcaneAssemblerBlockEntity.class)
            .buildUnregistered(AppliedThaumaturge.id("arcane_assembler"));

    private ATMenus() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("portable_essentia_cell"), PORTABLE_ESSENTIA_CELL);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("arcane_terminal"), ARCANE_TERMINAL);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("wireless_arcane_terminal"), WIRELESS_ARCANE_TERMINAL);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("arcane_inscriber"), ARCANE_INSCRIBER);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("arcane_assembler"), ARCANE_ASSEMBLER);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("essentia_import_bus"), ESSENTIA_IMPORT_BUS);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("essentia_export_bus"), ESSENTIA_EXPORT_BUS);
        Registry.register(BuiltInRegistries.MENU, AppliedThaumaturge.id("essentia_level_emitter"), ESSENTIA_LEVEL_EMITTER);
    }
}
