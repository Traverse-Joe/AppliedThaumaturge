package com.traverse.appliedthaumics.me.strategy;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public interface EssentiaEndpoint {
    AspectList contents();

    int insert(Holder<IAspect> aspect, int amount, boolean simulate);

    int extract(Holder<IAspect> aspect, int amount, boolean simulate);

    static Lookup lookup(ServerLevel level, BlockPos pos, Direction side) {
        return new Lookup(
                BlockCapabilityCache.create(EssentiaCapabilities.STORAGE, level, pos, side),
                BlockCapabilityCache.create(EssentiaCapabilities.TRANSPORT, level, pos, side),
                side);
    }

    final class Lookup {
        private final BlockCapabilityCache<IEssentiaStorage, Direction> storage;
        private final BlockCapabilityCache<IEssentiaTransport, Direction> transport;
        private final Direction side;

        private Lookup(BlockCapabilityCache<IEssentiaStorage, Direction> storage, BlockCapabilityCache<IEssentiaTransport, Direction> transport, Direction side) {
            this.storage = storage;
            this.transport = transport;
            this.side = side;
        }

        public ServerLevel level() {
            return storage.level();
        }

        @Nullable
        public EssentiaEndpoint find() {
            IEssentiaStorage s = storage.getCapability();
            if (s != null) {
                return new StorageEndpoint(s);
            }
            IEssentiaTransport t = transport.getCapability();
            if (t != null) {
                return new TransportEndpoint(t, side);
            }
            return null;
        }

        @Nullable
        public EssentiaEndpoint findStorage() {
            IEssentiaStorage s = storage.getCapability();
            return s == null ? null : new StorageEndpoint(s);
        }
    }

    record StorageEndpoint(IEssentiaStorage storage) implements EssentiaEndpoint {
        @Override
        public AspectList contents() {
            return storage.contents();
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, boolean simulate) {
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = storage.insert(aspect, amount, tx);
                if (!simulate && inserted > 0) {
                    tx.commit();
                }
                return inserted;
            }
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = storage.extract(aspect, amount, tx);
                if (!simulate && extracted > 0) {
                    tx.commit();
                }
                return extracted;
            }
        }
    }

    record TransportEndpoint(IEssentiaTransport transport, Direction side) implements EssentiaEndpoint {
        @Override
        public AspectList contents() {
            if (!transport.canOutputTo(side)) {
                return AspectList.EMPTY;
            }
            Holder<IAspect> type = transport.getEssentiaType(side);
            int amount = transport.getEssentiaAmount(side);
            return type == null || amount <= 0 ? AspectList.EMPTY : AspectList.EMPTY.add(type, amount);
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (!transport.canInputFrom(side)) {
                return 0;
            }
            return Math.max(0, transport.addEssentia(aspect, amount, side, simulate));
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (!transport.canOutputTo(side)) {
                return 0;
            }
            return Math.max(0, transport.takeEssentia(aspect, amount, side, simulate));
        }
    }
}
