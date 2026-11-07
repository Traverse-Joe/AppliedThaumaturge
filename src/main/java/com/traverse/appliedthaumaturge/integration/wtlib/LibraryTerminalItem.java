package com.traverse.appliedthaumaturge.integration.wtlib;

import appeng.menu.locator.ItemMenuHostLocator;
import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class LibraryTerminalItem extends ItemWT {
    private final boolean arcane;

    public LibraryTerminalItem(boolean arcane) {
        super();
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult result = useMode(player, hand, stack);
        return result != InteractionResult.PASS ? new InteractionResultHolder<>(result, stack) : super.use(level, player, hand);
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return arcane ? UseAnim.BOW : super.getUseAnimation(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (arcane) {
            WirelessArcaneTerminalItem.drainTick(this, level, entity, stack, remainingUseTicks);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (arcane) {
            WirelessArcaneTerminalItem.appendModeTooltip(this, stack, tooltip::add);
        } else {
            WirelessEssentiaTerminalItem.appendModeTooltip(this, stack, tooltip::add);
        }
    }

    public static final class Arcane extends LibraryTerminalItem implements IChanneledItem {
        public Arcane() {
            super(true);
        }
    }
}
