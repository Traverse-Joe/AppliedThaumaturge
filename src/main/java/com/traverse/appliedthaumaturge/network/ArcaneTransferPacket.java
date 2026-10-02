package com.traverse.appliedthaumaturge.network;

import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.menu.ArcaneRecipeTarget;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ArcaneTransferPacket(int containerId, List<List<ItemStack>> slots) implements CustomPacketPayload {
    public static final Type<ArcaneTransferPacket> TYPE = new Type<>(AppliedThaumaturge.id("arcane_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneTransferPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ArcaneTransferPacket::containerId,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(32)).apply(ByteBufCodecs.list(16)), ArcaneTransferPacket::slots,
            ArcaneTransferPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ArcaneTransferPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof ArcaneRecipeTarget target && context.player().containerMenu.containerId == packet.containerId()) {
            target.fillFromRecipe(packet.slots());
        }
    }
}
