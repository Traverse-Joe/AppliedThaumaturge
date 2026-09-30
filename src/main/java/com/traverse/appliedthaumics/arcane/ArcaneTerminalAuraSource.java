package com.traverse.appliedthaumics.arcane;

import appeng.api.parts.IPartHost;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import com.traverse.appliedthaumics.part.ArcaneTerminalPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class ArcaneTerminalAuraSource implements IWorkbenchAuraSource {
    @Override
    public int supply(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench workbench, int need, TransactionContext transaction) {
        BlockPos pos = context.blockPosition().orElse(null);
        ServerLevel level = context.level();
        if (pos == null || need <= 0) {
            return 0;
        }
        ArcaneTerminalPart terminal = findTerminal(level, pos, context);
        if (terminal == null) {
            return 0;
        }
        List<BlockPos> anchors = anchors(pos, terminal.hasChargingCard());
        float remaining = need;
        int share = Math.max(1, need / anchors.size());
        while (remaining > 0) {
            float drainedThisPass = 0;
            for (BlockPos anchor : anchors) {
                float drained = AuraHelper.drainVis(level, anchor, Math.min(share, remaining), transaction);
                drainedThisPass += drained;
                remaining -= drained;
                if (remaining <= 0) {
                    return need;
                }
            }
            if (drainedThisPass <= 0) {
                break;
            }
        }
        return need - (int) Math.ceil(Math.max(0, remaining));
    }

    @Nullable
    private static ArcaneTerminalPart findTerminal(ServerLevel level, BlockPos pos, ArcaneWorkbenchContext context) {
        if (!(level.getBlockEntity(pos) instanceof IPartHost host)) {
            return null;
        }
        for (Direction side : Direction.values()) {
            if (host.getPart(side) instanceof ArcaneTerminalPart terminal && terminal.getHostId().equals(context.hostIdentity())) {
                return terminal;
            }
        }
        return null;
    }

    public static List<BlockPos> anchors(BlockPos pos, boolean charged) {
        if (!charged) {
            return List.of(pos);
        }
        ChunkPos center = ChunkPos.containing(pos);
        List<BlockPos> anchors = new ArrayList<>(9);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                anchors.add(new ChunkPos(center.x() + x, center.z() + z).getMiddleBlockPosition(pos.getY()));
            }
        }
        return anchors;
    }
}
