package com.traverse.appliedthaumics.vis;

import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.traverse.appliedthaumics.part.VisRelayInterfacePart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public final class VisRelayCharging {
    private VisRelayCharging() {
    }

    public static boolean charge(ItemStack wand, @Nullable VisRelayInterfacePart source) {
        if (source == null || !(wand.getItem() instanceof ItemWand)) {
            return false;
        }
        boolean charged = false;
        for (var aspect : TCAspects.PRIMALS) {
            int room = Math.min(10, WandVisHelper.getMaxVis(wand) - WandVisHelper.getVis(wand, aspect));
            if (room <= 0) {
                continue;
            }
            int drained;
            try (Transaction tx = Transaction.openRoot()) {
                drained = source.drainCentivis(aspect, room, tx);
                if (drained > 0) {
                    tx.commit();
                }
            }
            if (drained > 0) {
                charged = true;
                WandVisHelper.addRealVis(wand, aspect, drained, true);
            }
        }
        return charged;
    }
}
