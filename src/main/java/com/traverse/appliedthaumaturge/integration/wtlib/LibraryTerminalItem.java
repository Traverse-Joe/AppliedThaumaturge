package com.traverse.appliedthaumaturge.integration.wtlib;

import appeng.menu.locator.ItemMenuHostLocator;
import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class LibraryTerminalItem extends ItemWT {
    private final boolean arcane;

    public LibraryTerminalItem(Properties properties, boolean arcane) {
        super(properties);
        this.arcane = arcane;
    }

    @Override
    public MenuType<?> getMenuType() {
        return arcane ? ATMenus.WIRELESS_ARCANE_TERMINAL : ATMenus.WIRELESS_ESSENTIA_TERMINAL;
    }

    @Override
    public MenuType<?> getMenuType(ItemMenuHostLocator locator, Player player) {
        return getMenuType();
    }

    @Override
    protected boolean checkPreconditions(ItemStack stack) {
        return super.checkPreconditions(stack) && !(arcane
                ? WirelessArcaneTerminalItem.isDrainMode(stack) : WirelessEssentiaTerminalItem.isAlchemyMode(stack));
    }

    @Override
    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubMenu) {
        return !player.level().isClientSide() && tryOpen(player, locator, returningFromSubMenu);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = useMode(player, hand, player.getItemInHand(hand));
        return result != InteractionResult.PASS ? result : super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : useMode(player, context.getHand(), stack);
    }

    private InteractionResult useMode(Player player, InteractionHand hand, ItemStack stack) {
        return arcane ? WirelessArcaneTerminalItem.useMode(this, player, hand, stack)
                : WirelessEssentiaTerminalItem.useMode(this, player, hand, stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return arcane ? 72000 : super.getUseDuration(stack, entity);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return arcane ? ItemUseAnimation.BOW : super.getUseAnimation(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (arcane) {
            WirelessArcaneTerminalItem.drainTick(this, level, entity, stack, remainingUseTicks);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (arcane) {
            WirelessArcaneTerminalItem.appendModeTooltip(this, stack, tooltip);
        } else {
            WirelessEssentiaTerminalItem.appendModeTooltip(this, stack, tooltip);
        }
    }

    public static final class Arcane extends LibraryTerminalItem implements IChanneledItem {
        public Arcane(Properties properties) {
            super(properties, true);
        }
    }
}
