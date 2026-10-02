package com.traverse.appliedthaumaturge.wand;

import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.mojang.serialization.Codec;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

public final class EntangledVisData extends SavedData {
    private static final Codec<Map<String, WandVis>> POOLS_CODEC = Codec.unboundedMap(Codec.STRING, WandVis.CODEC);
    public static final Codec<EntangledVisData> CODEC = POOLS_CODEC.xmap(EntangledVisData::fromStrings, EntangledVisData::toStrings);
    public static final SavedDataType<EntangledVisData> TYPE = new SavedDataType<>(
            AppliedThaumaturge.id("entangled_vis"), EntangledVisData::new, CODEC);

    private final Map<Long, WandVis> pools = new HashMap<>();

    public EntangledVisData() {
    }

    public static EntangledVisData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    @Nullable
    public WandVis pool(long id) {
        return pools.get(id);
    }

    public void setPool(long id, WandVis vis) {
        WandVis previous = pools.put(id, vis);
        if (!vis.equals(previous)) {
            setDirty();
        }
    }

    private static EntangledVisData fromStrings(Map<String, WandVis> raw) {
        EntangledVisData data = new EntangledVisData();
        raw.forEach((key, vis) -> {
            try {
                data.pools.put(Long.parseLong(key), vis);
            } catch (NumberFormatException ignored) {
            }
        });
        return data;
    }

    private Map<String, WandVis> toStrings() {
        Map<String, WandVis> raw = new HashMap<>();
        pools.forEach((id, vis) -> raw.put(Long.toString(id), vis));
        return raw;
    }
}
