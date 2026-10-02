package com.traverse.appliedthaumaturge;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.parts.automation.StackWorldBehaviors;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import com.mojang.logging.LogUtils;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalAuraSource;
import com.traverse.appliedthaumaturge.menu.ATSlotSemantics;
import com.traverse.appliedthaumaturge.network.ATNetwork;
import com.traverse.appliedthaumaturge.me.cell.CreativeEssentiaCellHandler;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaContainerItemStrategy;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaExportStrategy;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaExternalStorageStrategy;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaImportStrategy;
import com.traverse.appliedthaumaturge.registry.ATBlockEntities;
import com.traverse.appliedthaumaturge.registry.ATBlocks;
import com.traverse.appliedthaumaturge.registry.ATCreativeTabs;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATRegistryAliases;
import com.traverse.appliedthaumaturge.registry.ATWandParts;
import com.traverse.appliedthaumaturge.wand.EntangledWands;
import net.neoforged.neoforge.common.NeoForge;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.minecraft.core.registries.Registries;
import com.traverse.appliedthaumaturge.registry.ATUpgrades;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(AppliedThaumaturge.MODID)
public class AppliedThaumaturge {
    public static final String MODID = "appliedthaumaturge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppliedThaumaturge(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ATConfig.SPEC);
        ATSlotSemantics.ARCANE_CRYSTALS.id();
        ATWandParts.RODS.register(modBus);
        ATBlocks.BLOCKS.register(modBus);
        ATItems.ITEMS.register(modBus);
        ATDataComponents.COMPONENTS.register(modBus);
        ATBlockEntities.TYPES.register(modBus);
        ATCreativeTabs.TABS.register(modBus);
        modBus.addListener(this::onRegister);
        modBus.addListener(EventPriority.LOWEST, ATRegistryAliases::register);
        modBus.addListener(ATBlockEntities::registerCapabilities);
        modBus.addListener(com.traverse.appliedthaumaturge.vis.VisRelayHostSource::registerCapabilities);
        modBus.addListener(com.traverse.appliedthaumaturge.part.EssentiaExportBusPart::registerCapabilities);
        modBus.addListener((RegisterWorkbenchAuraSourcesEvent event) -> event.register(new ArcaneTerminalAuraSource()));
        modBus.addListener(ATNetwork::register);
        modBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(EntangledWands::onInfusionCrafted);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(AEKeyType.REGISTRY_KEY)) {
            AEKeyTypes.register(EssentiaKeyType.TYPE);
            AEKeyTypes.register(com.traverse.appliedthaumaturge.me.key.VisKeyType.TYPE);
        } else if (event.getRegistryKey().equals(Registries.MENU)) {
            ATMenus.register();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            StackWorldBehaviors.registerExternalStorageStrategy(EssentiaKeyType.TYPE, EssentiaExternalStorageStrategy::new);
            StackWorldBehaviors.registerImportStrategy(EssentiaKeyType.TYPE, EssentiaImportStrategy::new);
            StackWorldBehaviors.registerExportStrategy(EssentiaKeyType.TYPE, EssentiaExportStrategy::new);
            ContainerItemStrategy.register(EssentiaKeyType.TYPE, EssentiaKey.class, new EssentiaContainerItemStrategy());
            GenericSlotCapacities.register(EssentiaKeyType.TYPE, 1000L);
            StorageCells.addCellHandler(CreativeEssentiaCellHandler.INSTANCE);
            ATUpgrades.register();
            ATBlockEntities.registerRepresentativeItems();
        });
    }
}
