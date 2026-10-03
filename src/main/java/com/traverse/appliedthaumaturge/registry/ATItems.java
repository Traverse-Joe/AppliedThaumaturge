package com.traverse.appliedthaumaturge.registry;

import appeng.items.materials.StorageComponentItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.StorageTier;
import com.traverse.appliedthaumaturge.item.PortableEssentiaCellItem;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.item.CreativeEssentiaCellItem;
import com.traverse.appliedthaumaturge.item.KnowledgeCoreItem;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import appeng.api.parts.IPart;
import appeng.api.upgrades.Upgrades;
import appeng.api.parts.IPartItem;
import appeng.items.parts.PartItem;
import com.traverse.appliedthaumaturge.part.EssentiaExportBusPart;
import com.traverse.appliedthaumaturge.part.EssentiaImportBusPart;
import com.traverse.appliedthaumaturge.part.EssentiaLevelEmitterPart;
import com.traverse.appliedthaumaturge.part.EssentiaStorageBusPart;
import com.traverse.appliedthaumaturge.part.EssentiaTerminalPart;
import com.traverse.appliedthaumaturge.part.VisRelayInterfacePart;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.part.ArcaneInscriberPart;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import appeng.core.AEConfig;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedThaumaturge.MODID);

    public static final DeferredItem<Item> DIFFUSION_CORE = ITEMS.registerSimpleItem("diffusion_core");
    public static final DeferredItem<Item> COALESCENCE_CORE = ITEMS.registerSimpleItem("coalescence_core");
    public static final DeferredItem<Item> ESSENTIA_CELL_HOUSING = ITEMS.registerSimpleItem("essentia_cell_housing");

    public static final DeferredItem<StorageComponentItem> ESSENTIA_COMPONENT_1K = component("1k", 1);
    public static final DeferredItem<StorageComponentItem> ESSENTIA_COMPONENT_4K = component("4k", 4);
    public static final DeferredItem<StorageComponentItem> ESSENTIA_COMPONENT_16K = component("16k", 16);
    public static final DeferredItem<StorageComponentItem> ESSENTIA_COMPONENT_64K = component("64k", 64);

    public static final DeferredItem<BasicStorageCell> ESSENTIA_CELL_1K = cell("1k", 0.5, 1, 8);
    public static final DeferredItem<BasicStorageCell> ESSENTIA_CELL_4K = cell("4k", 1.0, 4, 32);
    public static final DeferredItem<BasicStorageCell> ESSENTIA_CELL_16K = cell("16k", 1.5, 16, 128);
    public static final DeferredItem<BasicStorageCell> ESSENTIA_CELL_64K = cell("64k", 2.0, 64, 512);
    public static final DeferredItem<CreativeEssentiaCellItem> ESSENTIA_CELL_CREATIVE = ITEMS.registerItem(
            "essentia_cell_creative", p -> new CreativeEssentiaCellItem(p.stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<PortableEssentiaCellItem> PORTABLE_ESSENTIA_CELL_1K = portableCell("1k", StorageTier.SIZE_1K);
    public static final DeferredItem<PortableEssentiaCellItem> PORTABLE_ESSENTIA_CELL_4K = portableCell("4k", StorageTier.SIZE_4K);
    public static final DeferredItem<PortableEssentiaCellItem> PORTABLE_ESSENTIA_CELL_16K = portableCell("16k", StorageTier.SIZE_16K);
    public static final DeferredItem<PortableEssentiaCellItem> PORTABLE_ESSENTIA_CELL_64K = portableCell("64k", StorageTier.SIZE_64K);

    public static final DeferredItem<Item> BLANK_KNOWLEDGE_CORE = ITEMS.registerSimpleItem("blank_knowledge_core", p -> p.stacksTo(16));
    public static final DeferredItem<KnowledgeCoreItem> KNOWLEDGE_CORE = ITEMS.registerItem("knowledge_core", p -> new KnowledgeCoreItem(p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<ItemWandRod> ENTANGLED_WAND_CORE = ITEMS.registerItem("entangled_wand_core",
            p -> new ItemWandRod(p.stacksTo(2).rarity(Rarity.RARE), ATWandParts.ENTANGLED));
    public static final DeferredItem<Item> UPGRADE_ARCANE = ITEMS.registerItem("upgrade_arcane", Upgrades::createUpgradeCardItem);
    public static final DeferredItem<Item> UPGRADE_ALCHEMY = ITEMS.registerItem("upgrade_alchemy", Upgrades::createUpgradeCardItem);
    public static final DeferredItem<Item> UPGRADE_NODE = ITEMS.registerItem("upgrade_node", Upgrades::createUpgradeCardItem);

    public static final DeferredItem<PartItem<EssentiaImportBusPart>> ESSENTIA_IMPORT_BUS = part("essentia_import_bus", EssentiaImportBusPart.class, EssentiaImportBusPart::new);
    public static final DeferredItem<PartItem<EssentiaExportBusPart>> ESSENTIA_EXPORT_BUS = part("essentia_export_bus", EssentiaExportBusPart.class, EssentiaExportBusPart::new);
    public static final DeferredItem<PartItem<EssentiaStorageBusPart>> ESSENTIA_STORAGE_BUS = part("essentia_storage_bus", EssentiaStorageBusPart.class, EssentiaStorageBusPart::new);
    public static final DeferredItem<PartItem<EssentiaLevelEmitterPart>> ESSENTIA_LEVEL_EMITTER = part("essentia_level_emitter", EssentiaLevelEmitterPart.class, EssentiaLevelEmitterPart::new);
    public static final DeferredItem<PartItem<VisRelayInterfacePart>> VIS_RELAY_INTERFACE = part("vis_relay_interface", VisRelayInterfacePart.class, VisRelayInterfacePart::new);

    public static final DeferredItem<PartItem<ArcaneTerminalPart>> ARCANE_TERMINAL = part("arcane_terminal", ArcaneTerminalPart.class, ArcaneTerminalPart::new);
    public static final DeferredItem<PartItem<ArcaneInscriberPart>> ARCANE_INSCRIBER = part("arcane_inscriber", ArcaneInscriberPart.class, ArcaneInscriberPart::new);
    public static final DeferredItem<PartItem<EssentiaTerminalPart>> ESSENTIA_TERMINAL = part("essentia_terminal", EssentiaTerminalPart.class, EssentiaTerminalPart::new);
    public static final DeferredItem<WirelessEssentiaTerminalItem> WIRELESS_ESSENTIA_TERMINAL = ITEMS.registerItem("wireless_essentia_terminal",
            p -> new WirelessEssentiaTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));
    public static final DeferredItem<WirelessArcaneTerminalItem> WIRELESS_ARCANE_TERMINAL = ITEMS.registerItem("wireless_arcane_terminal",
            p -> new WirelessArcaneTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1)));

    public static final int CELL_TYPES = 12;

    private static <P extends IPart> DeferredItem<PartItem<P>> part(String name, Class<P> partClass, Function<IPartItem<P>, P> factory) {
        return ITEMS.registerItem(name, p -> new PartItem<>(p, partClass, factory));
    }

    private ATItems() {
    }

    private static DeferredItem<StorageComponentItem> component(String size, int kb) {
        return ITEMS.registerItem("essentia_component_" + size, p -> new StorageComponentItem(p, kb));
    }

    private static DeferredItem<BasicStorageCell> cell(String size, double idleDrain, int kb, int bytesPerType) {
        return ITEMS.registerItem("essentia_cell_" + size,
                p -> new BasicStorageCell(p.stacksTo(1), idleDrain, kb, bytesPerType, CELL_TYPES, EssentiaKeyType.TYPE));
    }

    private static DeferredItem<PortableEssentiaCellItem> portableCell(String size, StorageTier tier) {
        return ITEMS.registerItem("portable_essentia_cell_" + size, p -> new PortableEssentiaCellItem(tier, p));
    }

    public static List<DeferredItem<PortableEssentiaCellItem>> portableCells() {
        return List.of(PORTABLE_ESSENTIA_CELL_1K, PORTABLE_ESSENTIA_CELL_4K, PORTABLE_ESSENTIA_CELL_16K, PORTABLE_ESSENTIA_CELL_64K);
    }

    public static List<DeferredItem<BasicStorageCell>> cells() {
        return List.of(ESSENTIA_CELL_1K, ESSENTIA_CELL_4K, ESSENTIA_CELL_16K, ESSENTIA_CELL_64K);
    }
}
