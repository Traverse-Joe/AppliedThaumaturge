package com.traverse.appliedthaumics.knowledge;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record KnowledgeCoreContents(List<KnowledgeRecipe> recipes) {
    public static final int CAPACITY = 9;
    public static final KnowledgeCoreContents EMPTY = new KnowledgeCoreContents(List.of());

    public static final Codec<KnowledgeCoreContents> CODEC = KnowledgeRecipe.CODEC.sizeLimitedListOf(CAPACITY)
            .xmap(KnowledgeCoreContents::new, KnowledgeCoreContents::recipes);
    public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeCoreContents> STREAM_CODEC =
            KnowledgeRecipe.STREAM_CODEC.apply(ByteBufCodecs.list(CAPACITY)).map(KnowledgeCoreContents::new, KnowledgeCoreContents::recipes);

    public KnowledgeCoreContents {
        recipes = List.copyOf(recipes);
    }

    public boolean isEmpty() {
        return recipes.isEmpty();
    }

    public boolean isFull() {
        return recipes.size() >= CAPACITY;
    }

    public int size() {
        return recipes.size();
    }

    public int indexOfOutput(ItemStack output) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).sameOutput(output)) {
                return i;
            }
        }
        return -1;
    }

    public KnowledgeCoreContents with(KnowledgeRecipe recipe) {
        List<KnowledgeRecipe> list = new ArrayList<>(recipes);
        int existing = indexOfOutput(recipe.output());
        if (existing >= 0) {
            list.set(existing, recipe);
        } else if (list.size() < CAPACITY) {
            list.add(recipe);
        } else {
            return this;
        }
        return new KnowledgeCoreContents(list);
    }

    public KnowledgeCoreContents without(int index) {
        if (index < 0 || index >= recipes.size()) {
            return this;
        }
        List<KnowledgeRecipe> list = new ArrayList<>(recipes);
        list.remove(index);
        return new KnowledgeCoreContents(list);
    }
}
