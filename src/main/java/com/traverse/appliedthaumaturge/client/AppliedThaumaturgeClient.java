package com.traverse.appliedthaumaturge.client;

import appeng.api.client.StorageCellModels;
import appeng.client.api.AEKeyRendering;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import appeng.client.InitScreens;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import com.traverse.appliedthaumaturge.menu.PortableEssentiaCellMenu;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@Mod(value = AppliedThaumaturge.MODID, dist = Dist.CLIENT)
public class AppliedThaumaturgeClient {
    public AppliedThaumaturgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerModels);
        modBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, ATMenus.PORTABLE_ESSENTIA_CELL, MEStorageScreen<PortableEssentiaCellMenu>::new, "/screens/terminals/portable_essentia_cell.json");
        InitScreens.register(event, ATMenus.ESSENTIA_IMPORT_BUS, IOBusScreen::new, "/screens/import_bus.json");
        InitScreens.register(event, ATMenus.ESSENTIA_EXPORT_BUS, IOBusScreen::new, "/screens/export_bus.json");
        InitScreens.register(event, ATMenus.ESSENTIA_LEVEL_EMITTER, StorageLevelEmitterScreen::new, "/screens/essentia_level_emitter.json");
        InitScreens.register(event, ATMenus.ARCANE_TERMINAL, ArcaneTermScreen::new, "/screens/terminals/arcane_terminal.json");
        InitScreens.register(event, ATMenus.WIRELESS_ARCANE_TERMINAL, ArcaneTermScreen::new, "/screens/terminals/arcane_terminal.json");
        InitScreens.register(event, ATMenus.ARCANE_INSCRIBER, ArcaneInscriberScreen::new, "/screens/terminals/arcane_inscriber.json");
        InitScreens.register(event, ATMenus.ARCANE_ASSEMBLER, ArcaneAssemblerScreen::new, "/screens/arcane_assembler.json");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AEKeyRendering.register(EssentiaKeyType.TYPE, EssentiaKey.class, EssentiaKeyRenderer.INSTANCE);
            AEKeyRendering.register(com.traverse.appliedthaumaturge.me.key.VisKeyType.TYPE,
                    com.traverse.appliedthaumaturge.me.key.VisKey.class, VisKeyRenderer.INSTANCE);
        });
    }

    private void registerModels(RegisterBlockStateModels event) {
        StorageCellModels.registerModel(ATItems.PORTABLE_ESSENTIA_CELL_1K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_1k"));
        StorageCellModels.registerModel(ATItems.PORTABLE_ESSENTIA_CELL_4K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_4k"));
        StorageCellModels.registerModel(ATItems.PORTABLE_ESSENTIA_CELL_16K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_16k"));
        StorageCellModels.registerModel(ATItems.PORTABLE_ESSENTIA_CELL_64K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_64k"));
        StorageCellModels.registerModel(ATItems.ESSENTIA_CELL_1K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_1k"));
        StorageCellModels.registerModel(ATItems.ESSENTIA_CELL_4K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_4k"));
        StorageCellModels.registerModel(ATItems.ESSENTIA_CELL_16K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_16k"));
        StorageCellModels.registerModel(ATItems.ESSENTIA_CELL_64K.get(), AppliedThaumaturge.id("block/drive/essentia_cell_64k"));
        StorageCellModels.registerModel(ATItems.ESSENTIA_CELL_CREATIVE.get(), AppliedThaumaturge.id("block/drive/essentia_cell_creative"));
    }
}
