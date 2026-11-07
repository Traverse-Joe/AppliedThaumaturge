package com.traverse.appliedthaumaturge.item;

import appeng.util.InteractionUtil;
import com.traverse.appliedthaumaturge.ATConfig;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeCoreContents;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeCores;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeRecipe;
import com.traverse.appliedthaumaturge.registry.ATItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class KnowledgeCoreItem extends Item {
    public KnowledgeCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return clear(stack, player) ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide()) : super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return clear(stack, context.getPlayer()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean clear(ItemStack stack, Player player) {
        if (player == null || !InteractionUtil.isInAlternateUseMode(player)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == stack) {
                inventory.setItem(slot, new ItemStack(ATItems.BLANK_KNOWLEDGE_CORE.get(), stack.getCount()));
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        KnowledgeCoreContents contents = KnowledgeCores.contents(stack);
        tooltip.add(Component.translatable("tooltip.appliedthaumaturge.knowledge_core.recipes", contents.size(), ATConfig.knowledgeCoreMaxRecipes())
                .withStyle(ChatFormatting.GRAY));
        for (KnowledgeRecipe recipe : contents.recipes()) {
            tooltip.add(Component.translatable("tooltip.appliedthaumaturge.knowledge_core.entry", recipe.output().getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
