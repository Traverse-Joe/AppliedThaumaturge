package com.traverse.appliedthaumaturge.client;

import appeng.api.client.AEKeyRendering;
import appeng.api.client.StorageCellModels;
import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.items.parts.PartItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.AbstractPortableCell;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import appeng.init.client.InitScreens;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import com.traverse.appliedthaumaturge.menu.PortableEssentiaCellMenu;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModList;
import com.traverse.appliedthaumaturge.menu.WirelessEssentiaTerminalMenu;
import com.traverse.appliedthaumaturge.integration.wtlib.client.LibraryTerminalScreens;

@Mod(value = AppliedThaumaturge.MODID, dist = Dist.CLIENT)
public class AppliedThaumaturgeClient {
    public AppliedThaumaturgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerModels);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::registerItemColors);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, ATMenus.PORTABLE_ESSENTIA_CELL, MEStorageScreen<PortableEssentiaCellMenu>::new, "/screens/terminals/portable_essentia_cell.json");
        InitScreens.register(event, ATMenus.ESSENTIA_IMPORT_BUS, IOBusScreen::new, "/screens/import_bus.json");
        InitScreens.register(event, ATMenus.ESSENTIA_EXPORT_BUS, IOBusScreen::new, "/screens/export_bus.json");
        InitScreens.register(event, ATMenus.ESSENTIA_LEVEL_EMITTER, StorageLevelEmitterScreen::new, "/screens/essentia_level_emitter.json");
        InitScreens.register(event, ATMenus.ARCANE_TERMINAL, ArcaneTermScreen::new, "/screens/terminals/arcane_terminal.json");
        if (ModList.get().isLoaded("ae2wtlib")) {
            LibraryTerminalScreens.register(event);
        } else {
            InitScreens.register(event, ATMenus.WIRELESS_ARCANE_TERMINAL, ArcaneTermScreen::new, "/screens/terminals/arcane_terminal.json");
            InitScreens.register(event, ATMenus.WIRELESS_ESSENTIA_TERMINAL,
                    MEStorageScreen<WirelessEssentiaTerminalMenu>::new, "/screens/terminals/terminal.json");
        }
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

    private void registerModels(ModelEvent.RegisterAdditional event) {
        registerCellModel(ATItems.PORTABLE_ESSENTIA_CELL_1K.get(), "essentia_cell_1k");
        registerCellModel(ATItems.PORTABLE_ESSENTIA_CELL_4K.get(), "essentia_cell_4k");
        registerCellModel(ATItems.PORTABLE_ESSENTIA_CELL_16K.get(), "essentia_cell_16k");
        registerCellModel(ATItems.PORTABLE_ESSENTIA_CELL_64K.get(), "essentia_cell_64k");
        registerCellModel(ATItems.ESSENTIA_CELL_1K.get(), "essentia_cell_1k");
        registerCellModel(ATItems.ESSENTIA_CELL_4K.get(), "essentia_cell_4k");
        registerCellModel(ATItems.ESSENTIA_CELL_16K.get(), "essentia_cell_16k");
        registerCellModel(ATItems.ESSENTIA_CELL_64K.get(), "essentia_cell_64k");
        registerCellModel(ATItems.ESSENTIA_CELL_CREATIVE.get(), "essentia_cell_creative");
    }

    private static void registerCellModel(ItemLike item, String model) {
        ResourceLocation id = AppliedThaumaturge.id("block/drive/" + model);
        if (!StorageCellModels.models().containsKey(item.asItem())) {
            StorageCellModels.registerModel(item, id);
        }
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (var cell : ATItems.cells()) {
            event.register(opaque(BasicStorageCell::getColor), cell.get());
        }
        event.register(opaque(BasicStorageCell::getColor), ATItems.ESSENTIA_CELL_CREATIVE.get());
        for (var cell : ATItems.portableCells()) {
            event.register(opaque(AbstractPortableCell::getColor), cell.get());
        }
        for (var item : ATItems.ITEMS.getEntries()) {
            if (item.get() instanceof PartItem<?>) {
                event.register(opaque(new StaticItemColor(AEColor.TRANSPARENT)), item.get());
            }
        }
    }

    private static ItemColor opaque(ItemColor color) {
        return (stack, tintIndex) -> FastColor.ARGB32.opaque(color.getColor(stack, tintIndex));
    }
}
