package com.traverse.appliedthaumaturge.me.key;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.mojang.serialization.MapCodec;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

public final class EssentiaKeyType extends AEKeyType {
    public static final EssentiaKeyType TYPE = new EssentiaKeyType();

    private EssentiaKeyType() {
        super(AppliedThaumaturge.id("essentia"), EssentiaKey.class, Component.translatable("key.appliedthaumaturge.essentia"));
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return EssentiaKey.MAP_CODEC;
    }

    @Override
    public AEKey readFromPacket(RegistryFriendlyByteBuf input) {
        return EssentiaKey.fromPacket(input);
    }

    @Override
    public int getAmountPerOperation() {
        return 4;
    }

    @Override
    public int getAmountPerByte() {
        return 8;
    }
}
