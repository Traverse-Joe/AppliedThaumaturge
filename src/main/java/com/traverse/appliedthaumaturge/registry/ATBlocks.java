package com.traverse.appliedthaumaturge.registry;

import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.assembler.ArcaneAssemblerBlock;
import com.traverse.appliedthaumaturge.block.EssentiaNetworkBlock;
import com.traverse.appliedthaumaturge.block.entity.EssentiaInterfaceBlockEntity;
import com.traverse.appliedthaumaturge.block.entity.InfusionProviderBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AppliedThaumaturge.MODID);

    public static final DeferredBlock<EssentiaNetworkBlock<EssentiaInterfaceBlockEntity>> ESSENTIA_INTERFACE =
            BLOCKS.registerBlock("essentia_interface", EssentiaNetworkBlock::new);
    public static final DeferredBlock<EssentiaNetworkBlock<InfusionProviderBlockEntity>> INFUSION_PROVIDER =
            BLOCKS.registerBlock("infusion_provider", EssentiaNetworkBlock::new);

    public static final DeferredBlock<ArcaneAssemblerBlock> ARCANE_ASSEMBLER =
            BLOCKS.registerBlock("arcane_assembler", ArcaneAssemblerBlock::new);

    public static final DeferredItem<BlockItem> ESSENTIA_INTERFACE_ITEM = ATItems.ITEMS.registerSimpleBlockItem(ESSENTIA_INTERFACE);
    public static final DeferredItem<BlockItem> INFUSION_PROVIDER_ITEM = ATItems.ITEMS.registerSimpleBlockItem(INFUSION_PROVIDER);
    public static final DeferredItem<BlockItem> ARCANE_ASSEMBLER_ITEM = ATItems.ITEMS.registerSimpleBlockItem(ARCANE_ASSEMBLER);

    private ATBlocks() {
    }
}
