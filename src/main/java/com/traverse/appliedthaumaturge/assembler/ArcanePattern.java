package com.traverse.appliedthaumaturge.assembler;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeCoreContents;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeRecipe;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATItems;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ArcanePattern implements IPatternDetails {
    private final KnowledgeRecipe knowledge;
    private final boolean usesWand;
    private final ArcaneCraftingRecipe recipe;
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final int[] inputGridSlots;
    private final List<Holder<IAspect>> crystalAspects;
    private final List<GenericStack> outputs;

    private ArcanePattern(KnowledgeRecipe knowledge, ArcaneCraftingRecipe recipe, boolean usesWand) {
        this.knowledge = knowledge;
        this.usesWand = usesWand;
        this.recipe = recipe;
        ItemStack definitionStack = new ItemStack(ATItems.KNOWLEDGE_CORE.get());
        definitionStack.set(ATDataComponents.KNOWLEDGE_CORE.get(), new KnowledgeCoreContents(List.of(knowledge)));
        this.definition = AEItemKey.of(definitionStack);

        List<IInput> inputList = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < KnowledgeRecipe.GRID_SIZE; slot++) {
            ItemStack stored = knowledge.grid().get(slot);
            if (stored.isEmpty()) {
                continue;
            }
            inputList.add(new Input(candidates(stored, knowledge.substitute()), 1));
            slots.add(slot);
        }
        this.crystalAspects = new ArrayList<>();
        for (ResourceKey<IAspect> primal : MenuArcaneWorkbench.PRIMAL_ORDER) {
            for (AspectInstance crystal : recipe.crystalCost().entries()) {
                if (!usesWand && crystal.aspect().is(primal) && crystal.amount() > 0) {
                    ItemStack stack = EssentiaCrystalFactory.of(crystal.aspect(), 1);
                    inputList.add(new Input(new GenericStack[]{new GenericStack(AEItemKey.of(stack), 1)}, crystal.amount()));
                    slots.add(-1);
                    crystalAspects.add(crystal.aspect());
                }
            }
        }
        this.inputs = inputList.toArray(IInput[]::new);
        this.inputGridSlots = slots.stream().mapToInt(Integer::intValue).toArray();
        this.outputs = List.of(GenericStack.fromItemStack(knowledge.output()));
    }

    @Nullable
    public static ArcanePattern create(KnowledgeRecipe knowledge, ArcaneCraftingRecipe recipe, boolean usesWand) {
        if (knowledge.output().isEmpty()) {
            return null;
        }
        return new ArcanePattern(knowledge, recipe, usesWand);
    }

    private GenericStack[] candidates(ItemStack stored, boolean substitute) {
        Set<AEItemKey> keys = new LinkedHashSet<>();
        keys.add(AEItemKey.of(stored));
        if (substitute) {
            for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
                if (ingredient.test(stored)) {
                    ingredient.items().map(Holder::value).map(Item::getDefaultInstance).map(AEItemKey::of).forEach(keys::add);
                    break;
                }
            }
        }
        return keys.stream().map(key -> new GenericStack(key, 1)).toArray(GenericStack[]::new);
    }

    public KnowledgeRecipe knowledge() {
        return knowledge;
    }

    public ArcaneCraftingRecipe recipe() {
        return recipe;
    }

    public int gridSlotOf(int inputIndex) {
        return inputGridSlots[inputIndex];
    }

    public boolean usesWand() {
        return usesWand;
    }

    public int crystalCount() {
        return crystalAspects.size();
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ArcanePattern other && other.definition.equals(definition) && other.usesWand == usesWand;
    }

    @Override
    public int hashCode() {
        return 31 * definition.hashCode() + Boolean.hashCode(usesWand);
    }

    private record Input(GenericStack[] possible, long multiplier) implements IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return possible;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            for (GenericStack stack : possible) {
                if (stack.what().equals(input)) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
