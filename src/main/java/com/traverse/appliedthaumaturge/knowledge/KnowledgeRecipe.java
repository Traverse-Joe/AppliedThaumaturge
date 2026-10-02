package com.traverse.appliedthaumaturge.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public record KnowledgeRecipe(ResourceKey<Recipe<?>> recipe, List<ItemStack> grid, ItemStack output, boolean substitute) {
    public static final int GRID_SIZE = 9;

    public static final Codec<KnowledgeRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(KnowledgeRecipe::recipe),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("grid").forGetter(KnowledgeRecipe::grid),
            ItemStack.CODEC.fieldOf("output").forGetter(KnowledgeRecipe::output),
            Codec.BOOL.optionalFieldOf("substitute", false).forGetter(KnowledgeRecipe::substitute)
    ).apply(builder, KnowledgeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeRecipe> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.RECIPE), KnowledgeRecipe::recipe,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(GRID_SIZE)), KnowledgeRecipe::grid,
            ItemStack.STREAM_CODEC, KnowledgeRecipe::output,
            ByteBufCodecs.BOOL, KnowledgeRecipe::substitute,
            KnowledgeRecipe::new);

    public KnowledgeRecipe {
        List<ItemStack> normalized = new ArrayList<>(GRID_SIZE);
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = i < grid.size() ? grid.get(i) : ItemStack.EMPTY;
            normalized.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
        grid = List.copyOf(normalized);
        output = output.copy();
    }

    public ItemStack output() {
        return output.copy();
    }

    public boolean sameOutput(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(output, stack);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof KnowledgeRecipe other
                && substitute == other.substitute
                && recipe.equals(other.recipe)
                && ItemStack.listMatches(grid, other.grid)
                && ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * recipe.hashCode() + ItemStack.hashStackList(grid)) + ItemStack.hashItemAndComponents(output)) + Boolean.hashCode(substitute);
    }
}
