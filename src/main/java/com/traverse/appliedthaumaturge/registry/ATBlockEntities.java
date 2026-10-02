package com.traverse.appliedthaumaturge.registry;

import appeng.api.AECapabilities;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.assembler.ArcaneAssemblerBlockEntity;
import com.traverse.appliedthaumaturge.block.EssentiaNetworkBlock;
import com.traverse.appliedthaumaturge.block.entity.EssentiaInterfaceBlockEntity;
import com.traverse.appliedthaumaturge.block.entity.EssentiaNetworkBlockEntity;
import com.traverse.appliedthaumaturge.block.entity.InfusionProviderBlockEntity;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppliedThaumaturge.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssentiaInterfaceBlockEntity>> ESSENTIA_INTERFACE =
            TYPES.register("essentia_interface", () -> create(EssentiaInterfaceBlockEntity.class, EssentiaInterfaceBlockEntity::new, ATBlocks.ESSENTIA_INTERFACE::get));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionProviderBlockEntity>> INFUSION_PROVIDER =
            TYPES.register("infusion_provider", () -> create(InfusionProviderBlockEntity.class, InfusionProviderBlockEntity::new, ATBlocks.INFUSION_PROVIDER::get));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneAssemblerBlockEntity>> ARCANE_ASSEMBLER =
            TYPES.register("arcane_assembler", () -> create(ArcaneAssemblerBlockEntity.class, ArcaneAssemblerBlockEntity::new, ATBlocks.ARCANE_ASSEMBLER::get));

    private ATBlockEntities() {
    }

    private static <T extends EssentiaNetworkBlockEntity> BlockEntityType<T> create(
            Class<T> clazz, BiFunction<BlockPos, BlockState, T> factory, Supplier<EssentiaNetworkBlock<T>> block) {
        BlockEntityType<T> type = new BlockEntityType<>(factory::apply, Set.of(block.get()));
        BlockEntityTicker<T> serverTicker = ServerTickingBlockEntity.class.isAssignableFrom(clazz)
                ? (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick()
                : null;
        block.get().setBlockEntity(clazz, type, null, serverTicker);
        return type;
    }

    public static void registerRepresentativeItems() {
        AEBaseBlockEntity.registerBlockEntityItem(ESSENTIA_INTERFACE.get(), ATBlocks.ESSENTIA_INTERFACE_ITEM.get());
        AEBaseBlockEntity.registerBlockEntityItem(INFUSION_PROVIDER.get(), ATBlocks.INFUSION_PROVIDER_ITEM.get());
        AEBaseBlockEntity.registerBlockEntityItem(ARCANE_ASSEMBLER.get(), ATBlocks.ARCANE_ASSEMBLER_ITEM.get());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ESSENTIA_INTERFACE.get(), (be, side) -> be);
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, INFUSION_PROVIDER.get(), (be, side) -> be);
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ARCANE_ASSEMBLER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, ESSENTIA_INTERFACE.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, INFUSION_PROVIDER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.STORAGE, ESSENTIA_INTERFACE.get(), (be, side) -> be.storage());
        event.registerBlockEntity(EssentiaCapabilities.STORAGE, INFUSION_PROVIDER.get(), (be, side) -> be.storage());
    }
}
