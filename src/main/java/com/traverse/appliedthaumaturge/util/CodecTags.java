package com.traverse.appliedthaumaturge.util;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public final class CodecTags {
    private CodecTags() {
    }

    public static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec, HolderLookup.Provider registries) {
        if (!tag.contains(key)) {
            return Optional.empty();
        }
        return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(key)).result();
    }

    public static <T> void write(CompoundTag tag, String key, Codec<T> codec, T value, HolderLookup.Provider registries) {
        codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value).result().ifPresent(encoded -> tag.put(key, encoded));
    }
}
