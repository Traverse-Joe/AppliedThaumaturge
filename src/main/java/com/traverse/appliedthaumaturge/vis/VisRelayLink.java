package com.traverse.appliedthaumaturge.vis;

import appeng.api.ids.AEComponents;
import appeng.api.parts.IPartHost;
import appeng.api.networking.IManagedGridNode;
import appeng.parts.AEBasePart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.traverse.appliedthaumaturge.part.VisRelayInterfacePart;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record VisRelayLink(Identifier dimension, BlockPos position, Direction side, String sourceId) {
    public static final Codec<VisRelayLink> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(VisRelayLink::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(VisRelayLink::position),
            Direction.CODEC.fieldOf("side").forGetter(VisRelayLink::side),
            Codec.STRING.fieldOf("source_id").forGetter(VisRelayLink::sourceId)
    ).apply(builder, VisRelayLink::new));

    public static VisRelayLink of(VisRelayInterfacePart part) {
        return new VisRelayLink(part.getLevel().dimension().identifier(), part.getBlockEntity().getBlockPos(),
                part.getSide(), part.getSourceId());
    }

    public Map<String, String> cardSettings() {
        return Map.of("vis_dimension", dimension.toString(), "vis_position", Long.toString(position.asLong()),
                "vis_side", side.getSerializedName(), "vis_source_id", sourceId);
    }

    @Nullable
    public static VisRelayLink fromCard(ItemStack card) {
        Map<String, String> settings = card.get(AEComponents.EXPORTED_SETTINGS);
        if (settings == null || !settings.containsKey("vis_source_id")) {
            return null;
        }
        try {
            Direction side = Direction.byName(settings.get("vis_side"));
            if (side == null) {
                return null;
            }
            return new VisRelayLink(Identifier.parse(settings.get("vis_dimension")),
                    BlockPos.of(Long.parseLong(settings.get("vis_position"))), side, settings.get("vis_source_id"));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    @Nullable
    public VisRelayInterfacePart resolve(AEBasePart target) {
        return resolve(target.getLevel(), target.getMainNode());
    }

    @Nullable
    public VisRelayInterfacePart resolve(@Nullable Level targetLevel, IManagedGridNode targetNode) {
        if (!(targetLevel instanceof ServerLevel level) || !targetNode.isActive()) {
            return null;
        }
        ServerLevel sourceLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (sourceLevel == null || !sourceLevel.hasChunkAt(position)
                || !(sourceLevel.getBlockEntity(position) instanceof IPartHost host)
                || !(host.getPart(side) instanceof VisRelayInterfacePart source)
                || !sourceId.equals(source.getSourceId()) || source.getMainNode() == targetNode || source.isOutput()
                || !source.getMainNode().isActive()
                || source.getMainNode().getGrid() != targetNode.getGrid()) {
            return null;
        }
        return source;
    }
}
