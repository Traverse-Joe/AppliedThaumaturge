package com.traverse.appliedthaumics.part;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.PlayerSource;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class EssentiaMonitorInteraction {
    private EssentiaMonitorInteraction() {
    }

    public static void empty(IGrid grid, IActionHost host, Player player, InteractionHand hand) {
        ItemAccess access = ItemAccess.forPlayerSlot(player, hand == InteractionHand.MAIN_HAND
                ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND);
        IEssentiaItemStorage container = access.getResource().toStack(1)
                .getCapability(EssentiaCapabilities.ITEM_STORAGE);
        if (container == null) {
            return;
        }
        var contents = container.contents().entries().stream().filter(entry -> entry.amount() > 0)
                .findFirst().orElse(null);
        if (contents == null) {
            return;
        }
        var aspect = contents.aspect();
        EssentiaKey key = EssentiaKey.of(aspect);
        if (key == null) {
            return;
        }
        int stored = container.contents().amountOf(aspect);
        if (stored <= 0) {
            return;
        }
        var inventory = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();
        var source = new PlayerSource(player, host);
        int accepted = (int) StorageHelper.poweredInsert(energy, inventory, key, stored, source,
                Actionable.SIMULATE);
        if (accepted <= 0) {
            return;
        }
        var result = container.extract(aspect, accepted);
        int moved = result.amountMoved();
        if (moved <= 0 || moved > accepted || result.resultingStack().isEmpty()) {
            return;
        }
        ItemResource emptied = ItemResource.of(result.resultingStack());
        try (Transaction tx = Transaction.openRoot()) {
            if (access.exchange(emptied, 1, tx) != 1) {
                return;
            }
        }
        long inserted = StorageHelper.poweredInsert(energy, inventory, key, moved, source);
        if (inserted != moved) {
            if (inserted > 0) {
                inventory.extract(key, inserted, Actionable.MODULATE, source);
            }
            return;
        }
        boolean exchanged;
        try (Transaction tx = Transaction.openRoot()) {
            exchanged = access.exchange(emptied, 1, tx) == 1;
            if (exchanged) {
                tx.commit();
            }
        }
        if (!exchanged) {
            inventory.extract(key, inserted, Actionable.MODULATE, source);
            return;
        }
        container.playTransferFeedback(player, IEssentiaItemStorage.TransferDirection.DRAIN);
        player.containerMenu.broadcastChanges();
    }

    public static void fill(IGrid grid, IActionHost host, Player player, InteractionHand hand, EssentiaKey key) {
        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand);
        IEssentiaItemStorage container = access.getResource().toStack(1)
                .getCapability(EssentiaCapabilities.ITEM_STORAGE);
        var aspect = key.holderOrNull(player.level().registryAccess());
        if (container == null || aspect == null || !container.canInsert(aspect)) {
            return;
        }
        int space = container.capacity(aspect) - container.contents().totalAmount();
        if (space <= 0) {
            return;
        }
        var inventory = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();
        var source = new PlayerSource(player, host);
        int available = (int) StorageHelper.poweredExtraction(energy, inventory, key, space, source,
                Actionable.SIMULATE);
        if (available <= 0) {
            return;
        }
        var result = container.insert(aspect, available);
        int moved = result.amountMoved();
        if (moved <= 0 || moved > available || result.resultingStack().isEmpty()) {
            return;
        }

        ItemResource filled = ItemResource.of(result.resultingStack());
        try (Transaction tx = Transaction.openRoot()) {
            if (access.exchange(filled, 1, tx) != 1) {
                return;
            }
        }
        long extracted = StorageHelper.poweredExtraction(energy, inventory, key, moved, source);
        if (extracted != moved) {
            if (extracted > 0) {
                inventory.insert(key, extracted, Actionable.MODULATE, source);
            }
            return;
        }
        boolean exchanged;
        try (Transaction tx = Transaction.openRoot()) {
            exchanged = access.exchange(filled, 1, tx) == 1;
            if (exchanged) {
                tx.commit();
            }
        }
        if (!exchanged) {
            inventory.insert(key, extracted, Actionable.MODULATE, source);
            return;
        }
        container.playTransferFeedback(player, IEssentiaItemStorage.TransferDirection.FILL);
        player.containerMenu.broadcastChanges();
    }
}
