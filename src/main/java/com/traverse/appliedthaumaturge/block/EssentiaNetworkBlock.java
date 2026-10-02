package com.traverse.appliedthaumaturge.block;

import appeng.block.AEBaseEntityBlock;
import com.traverse.appliedthaumaturge.block.entity.EssentiaNetworkBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class EssentiaNetworkBlock<T extends EssentiaNetworkBlockEntity> extends AEBaseEntityBlock<T> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public EssentiaNetworkBlock(Properties props) {
        super(metalProps(props));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, T be) {
        return currentState.setValue(ACTIVE, be.isNetworkActive());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}
