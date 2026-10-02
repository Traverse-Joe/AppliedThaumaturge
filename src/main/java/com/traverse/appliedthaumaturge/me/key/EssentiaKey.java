package com.traverse.appliedthaumaturge.me.key;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public final class EssentiaKey extends AEKey {
    public static final MapCodec<EssentiaKey> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceKey.codec(IAspect.REGISTRY_KEY).fieldOf("aspect").forGetter(EssentiaKey::getAspect)
    ).apply(builder, EssentiaKey::of));

    private final ResourceKey<IAspect> aspect;

    private EssentiaKey(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    public static EssentiaKey of(ResourceKey<IAspect> aspect) {
        return new EssentiaKey(aspect);
    }

    @Nullable
    public static EssentiaKey of(@Nullable Holder<IAspect> aspect) {
        if (aspect == null) {
            return null;
        }
        return aspect.unwrapKey().map(EssentiaKey::new).orElse(null);
    }

    public ResourceKey<IAspect> getAspect() {
        return aspect;
    }

    public Optional<Holder.Reference<IAspect>> holder(HolderLookup.Provider registries) {
        return registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(aspect));
    }

    @Nullable
    public Holder<IAspect> holderOrNull(HolderLookup.Provider registries) {
        return holder(registries).orElse(null);
    }

    public static EssentiaKey fromPacket(RegistryFriendlyByteBuf buf) {
        return new EssentiaKey(ResourceKey.create(IAspect.REGISTRY_KEY, buf.readIdentifier()));
    }

    @Override
    public AEKeyType getType() {
        return EssentiaKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public void toTag(ValueOutput output) {
        output.store(MAP_CODEC, this);
    }

    @Override
    public Object getPrimaryKey() {
        return aspect;
    }

    @Override
    public Identifier getId() {
        return aspect.identifier();
    }

    @Override
    public String getModId() {
        return aspect.identifier().getNamespace();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        data.writeIdentifier(aspect.identifier());
    }

    @Override
    protected Component computeDisplayName() {
        Identifier id = aspect.identifier();
        return Component.translatable("aspect." + id.getNamespace() + "." + id.getPath());
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof EssentiaKey other && other.aspect.equals(aspect);
    }

    @Override
    public int hashCode() {
        return aspect.hashCode();
    }

    @Override
    public String toString() {
        return "EssentiaKey{" + aspect.identifier() + "}";
    }
}
