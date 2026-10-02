package com.traverse.appliedthaumaturge.me.strategy;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class EssentiaExternalStorageStrategy implements ExternalStorageStrategy {
    private final EssentiaEndpoint.Lookup lookup;
    private final ServerLevel level;

    public EssentiaExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.lookup = EssentiaEndpoint.lookup(level, fromPos, fromSide);
        this.level = level;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        EssentiaEndpoint endpoint = lookup.findStorage();
        if (endpoint == null) {
            return null;
        }
        return new Wrapper(endpoint, level, injectOrExtractCallback);
    }

    private record Wrapper(EssentiaEndpoint endpoint, ServerLevel level, Runnable callback) implements MEStorage {
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof EssentiaKey key)) {
                return 0;
            }
            Holder<IAspect> aspect = key.holderOrNull(level.registryAccess());
            if (aspect == null) {
                return 0;
            }
            int inserted = endpoint.insert(aspect, clamp(amount), mode.isSimulate());
            if (inserted > 0 && mode == Actionable.MODULATE) {
                callback.run();
            }
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof EssentiaKey key)) {
                return 0;
            }
            Holder<IAspect> aspect = key.holderOrNull(level.registryAccess());
            if (aspect == null) {
                return 0;
            }
            int extracted = endpoint.extract(aspect, clamp(amount), mode.isSimulate());
            if (extracted > 0 && mode == Actionable.MODULATE) {
                callback.run();
            }
            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (AspectInstance entry : endpoint.contents().entries()) {
                EssentiaKey key = EssentiaKey.of(entry.aspect());
                if (key != null && entry.amount() > 0) {
                    out.add(key, entry.amount());
                }
            }
        }

        @Override
        public Component getDescription() {
            return GuiText.ExternalStorage.text(EssentiaKeyType.TYPE.getDescription());
        }
    }

    static int clamp(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, amount));
    }
}
