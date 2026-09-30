package com.traverse.appliedthaumics.me.key;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.mojang.serialization.MapCodec;
import com.traverse.appliedthaumics.AppliedThaumics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

public final class VisKeyType extends AEKeyType {
    public static final VisKeyType TYPE = new VisKeyType();

    private VisKeyType() {
        super(AppliedThaumics.id("vis"), VisKey.class, Component.translatable("key.appliedthaumics.vis"));
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return VisKey.MAP_CODEC;
    }

    @Override
    public AEKey readFromPacket(RegistryFriendlyByteBuf input) {
        return VisKey.fromPacket(input);
    }

    @Override
    public int getAmountPerUnit() {
        return 100;
    }

}
