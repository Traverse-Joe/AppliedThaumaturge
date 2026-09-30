package com.traverse.appliedthaumics;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageCells;
import appeng.parts.automation.StackWorldBehaviors;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import com.mojang.logging.LogUtils;
import com.traverse.appliedthaumics.arcane.ArcaneTerminalAuraSource;
import com.traverse.appliedthaumics.menu.ATSlotSemantics;
import com.traverse.appliedthaumics.network.ATNetwork;
import com.traverse.appliedthaumics.me.cell.CreativeEssentiaCellHandler;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import com.traverse.appliedthaumics.me.strategy.EssentiaContainerItemStrategy;
import com.traverse.appliedthaumics.me.strategy.EssentiaExportStrategy;
import com.traverse.appliedthaumics.me.strategy.EssentiaExternalStorageStrategy;
import com.traverse.appliedthaumics.me.strategy.EssentiaImportStrategy;
import com.traverse.appliedthaumics.registry.ATBlockEntities;
import com.traverse.appliedthaumics.registry.ATBlocks;
import com.traverse.appliedthaumics.registry.ATCreativeTabs;
import com.traverse.appliedthaumics.registry.ATDataComponents;
import com.traverse.appliedthaumics.registry.ATWandParts;
import com.traverse.appliedthaumics.wand.EntangledWands;
import net.neoforged.neoforge.common.NeoForge;
import com.traverse.appliedthaumics.registry.ATItems;
import com.traverse.appliedthaumics.registry.ATMenus;
import net.minecraft.core.registries.Registries;
import com.traverse.appliedthaumics.registry.ATUpgrades;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(AppliedThaumics.MODID)
public class AppliedThaumics {
    public static final String MODID = "appliedthaumics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppliedThaumics(IEventBus modBus, ModContainer container) {
        ATSlotSemantics.ARCANE_CRYSTALS.id();
        ATWandParts.RODS.register(modBus);
        ATBlocks.BLOCKS.register(modBus);
        ATItems.ITEMS.register(modBus);
        ATDataComponents.COMPONENTS.register(modBus);
        ATBlockEntities.TYPES.register(modBus);
        ATCreativeTabs.TABS.register(modBus);
        modBus.addListener(this::onRegister);
        modBus.addListener(ATBlockEntities::registerCapabilities);
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
            AEKeyTypes.register(com.traverse.appliedthaumics.me.key.VisKeyType.TYPE);
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
