package com.traverse.appliedthaumaturge.arcane;

import appeng.api.parts.IPartHost;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.ATConfig;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
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
        ArcaneTerminalHost terminal = findTerminal(level, pos, context);
        if (terminal == null && player.containerMenu instanceof ArcaneTermMenu menu
                && menu.getArcaneHost() instanceof WirelessArcaneTerminalItem.Host wireless
                && wireless.matchesContext(context) && menu.canCraft()) {
            terminal = wireless;
        }
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
        int radius = charged ? ATConfig.arcaneChargingCardRadiusChunks() : 0;
        if (radius == 0) {
            return List.of(pos);
        }
        ChunkPos center = ChunkPos.containing(pos);
        int diameter = radius * 2 + 1;
        List<BlockPos> anchors = new ArrayList<>(diameter * diameter);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                anchors.add(new ChunkPos(center.x() + x, center.z() + z).getMiddleBlockPosition(pos.getY()));
            }
        }
        return anchors;
    }
}
