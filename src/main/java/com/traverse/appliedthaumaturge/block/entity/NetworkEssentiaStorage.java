package com.traverse.appliedthaumaturge.block.entity;

import appeng.api.config.Actionable;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class NetworkEssentiaStorage extends SnapshotJournal<Map<Holder<IAspect>, Integer>> implements IEssentiaStorage {
    private final EssentiaNetworkBlockEntity host;
    private Map<Holder<IAspect>, Integer> pending = new HashMap<>();
    private long revision;

    NetworkEssentiaStorage(EssentiaNetworkBlockEntity host) {
        this.host = host;
    }

    @Override
    public AspectList contents() {
        AspectList out = host.networkContents();
        for (Map.Entry<Holder<IAspect>, Integer> entry : pending.entrySet()) {
            int delta = entry.getValue();
            if (delta > 0) {
                out = out.add(entry.getKey(), delta);
            } else if (delta < 0) {
                out = out.reduce(entry.getKey(), -delta);
            }
        }
        return out;
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = host.insertIntoNetwork(aspect, amount + Math.max(0, pending.getOrDefault(aspect, 0)), Actionable.SIMULATE)
                - Math.max(0, pending.getOrDefault(aspect, 0));
        accepted = Math.max(0, Math.min(amount, accepted));
        if (accepted > 0) {
            updateSnapshots(transaction);
            pending.merge(aspect, accepted, Integer::sum);
        }
        return accepted;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        if (amount <= 0) {
            return 0;
        }
        long available = host.networkAmount(aspect) + pending.getOrDefault(aspect, 0);
        int extracted = (int) Math.max(0, Math.min(amount, available));
        if (extracted > 0) {
            updateSnapshots(transaction);
            pending.merge(aspect, -extracted, Integer::sum);
        }
        return extracted;
    }

    @Override
    public long contentRevision() {
        return revision + host.networkRevision();
    }

    @Override
    protected Map<Holder<IAspect>, Integer> createSnapshot() {
        return new HashMap<>(pending);
    }

    @Override
    protected void revertToSnapshot(Map<Holder<IAspect>, Integer> snapshot) {
        pending = snapshot;
    }

    @Override
    protected void onRootCommit(Map<Holder<IAspect>, Integer> originalState) {
        for (Map.Entry<Holder<IAspect>, Integer> entry : pending.entrySet()) {
            int delta = entry.getValue();
            if (delta > 0) {
                host.insertIntoNetwork(entry.getKey(), delta, Actionable.MODULATE);
            } else if (delta < 0) {
                host.extractFromNetwork(entry.getKey(), -delta, Actionable.MODULATE);
            }
        }
        pending.clear();
        revision++;
    }
}
