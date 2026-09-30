package com.traverse.appliedthaumics.assembler;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.traverse.appliedthaumics.block.EssentiaNetworkBlock;
import com.traverse.appliedthaumics.registry.ATMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ArcaneAssemblerBlock extends EssentiaNetworkBlock<ArcaneAssemblerBlockEntity> {
    public ArcaneAssemblerBlock(Properties props) {
        super(props.noOcclusion());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ArcaneAssemblerBlockEntity be = getBlockEntity(level, pos);
        if (be == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            MenuOpener.open(ATMenus.ARCANE_ASSEMBLER, player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS;
    }
}
