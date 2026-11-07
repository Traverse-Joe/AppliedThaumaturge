package com.traverse.appliedthaumaturge.block.entity;

import appeng.api.config.Actionable;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import net.minecraft.core.Holder;

final class NetworkEssentiaStorage implements IEssentiaStorage {
    private final EssentiaNetworkBlockEntity host;
    private long revision;

    NetworkEssentiaStorage(EssentiaNetworkBlockEntity host) {
        this.host = host;
    }

    @Override
    public AspectList contents() {
        return host.networkContents();
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.max(0, Math.min(amount, host.insertIntoNetwork(aspect, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE)));
        if (!simulate && accepted > 0) {
            revision++;
        }
        return accepted;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int extracted = Math.max(0, Math.min(amount, host.extractFromNetwork(aspect, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE)));
        if (!simulate && extracted > 0) {
            revision++;
        }
        return extracted;
    }

    @Override
    public long contentRevision() {
        return revision + host.networkRevision();
    }
}
