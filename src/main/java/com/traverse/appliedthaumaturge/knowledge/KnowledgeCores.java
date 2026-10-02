package com.traverse.appliedthaumaturge.knowledge;

import com.traverse.appliedthaumaturge.item.KnowledgeCoreItem;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATItems;
import net.minecraft.world.item.ItemStack;

public final class KnowledgeCores {
    private KnowledgeCores() {
    }

    public static boolean isBlank(ItemStack stack) {
        return stack.is(ATItems.BLANK_KNOWLEDGE_CORE.get());
    }

    public static boolean isCore(ItemStack stack) {
        return isBlank(stack) || stack.getItem() instanceof KnowledgeCoreItem;
    }

    public static KnowledgeCoreContents contents(ItemStack stack) {
        return stack.getOrDefault(ATDataComponents.KNOWLEDGE_CORE.get(), KnowledgeCoreContents.EMPTY);
    }

    public static boolean canWrite(ItemStack core, KnowledgeRecipe recipe) {
        if (!isCore(core)) {
            return false;
        }
        KnowledgeCoreContents contents = contents(core);
        return !contents.isFull() || contents.indexOfOutput(recipe.output()) >= 0;
    }

    public static ItemStack write(ItemStack core, KnowledgeRecipe recipe) {
        if (!canWrite(core, recipe)) {
            return core;
        }
        return withContents(core, contents(core).with(recipe));
    }

    public static ItemStack erase(ItemStack core, int index) {
        if (!isCore(core)) {
            return core;
        }
        return withContents(core, contents(core).without(index));
    }

    private static ItemStack withContents(ItemStack core, KnowledgeCoreContents contents) {
        ItemStack result;
        if (contents.isEmpty()) {
            result = new ItemStack(ATItems.BLANK_KNOWLEDGE_CORE.get(), core.getCount());
        } else {
            result = new ItemStack(ATItems.KNOWLEDGE_CORE.get(), core.getCount());
            result.set(ATDataComponents.KNOWLEDGE_CORE.get(), contents);
        }
        return result;
    }
}
