package com.traverse.appliedthaumaturge.item;

import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalHost;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import com.traverse.appliedthaumaturge.registry.ATTerminalHotkeys;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class WirelessArcaneTerminalItem extends WirelessTerminalItem implements IChanneledItem {
    public WirelessArcaneTerminalItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {
        return ATMenus.WIRELESS_ARCANE_TERMINAL;
    }

    @Override
    @Nullable
    public Host getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        if (isDrainMode(locator.locateItem(player))) {
            return null;
        }
        return new Host(this, player, locator);
    }

    @Override
    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubMenu) {
        if (isDrainMode(locator.locateItem(player))) {
            return false;
        }
        return super.openFromInventory(player, locator, returningFromSubMenu);
    }

    public static boolean isDrainMode(ItemStack terminal) {
        return terminal.is(ATItems.WIRELESS_ARCANE_TERMINAL.get())
                && terminal.getOrDefault(ATDataComponents.DRAIN_MODE.get(), false);
    }

    private static boolean hasDrainUpgrade(WirelessTerminalItem item, ItemStack terminal) {
        return item.getUpgrades(terminal).isInstalled(ATItems.UPGRADE_NODE);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = useMode(this, player, hand, player.getItemInHand(hand));
        if (result != InteractionResult.PASS) {
            return result;
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : useMode(this, player, context.getHand(), stack);
    }

    public static InteractionResult useMode(WirelessTerminalItem item, Player player, InteractionHand hand, ItemStack terminal) {
        boolean draining = isDrainMode(terminal);
        boolean hasUpgrade = hasDrainUpgrade(item, terminal);
        if (player.isShiftKeyDown() && (hasUpgrade || draining)) {
            if (!player.level().isClientSide()) {
                terminal.set(ATDataComponents.DRAIN_MODE.get(), !draining);
                player.getInventory().setChanged();
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 0.4F, draining ? 0.9F : 1.2F);
            }
            return InteractionResult.CONSUME;
        }
        if (draining) {
            if (hasUpgrade) {
                startDraining(player, hand, terminal);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static boolean startDraining(Player player, InteractionHand hand, ItemStack terminal) {
        if (targetedNode(player) == null
                || !canDrainWith(loadArcaneInventory(terminal).getStackInSlot(ArcaneTerminalPart.WAND_SLOT))) {
            return false;
        }
        player.startUsingItem(hand);
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack terminal, int remainingUseTicks) {
        drainTick(this, level, entity, terminal, remainingUseTicks);
    }

    public static void drainTick(WirelessTerminalItem item, Level level, LivingEntity entity, ItemStack terminal, int remainingUseTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }
        BlockEntityNode node = targetedNode(player);
        if (!isDrainMode(terminal) || !hasDrainUpgrade(item, terminal) || node == null) {
            player.stopUsingItem();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel) || remainingUseTicks % 5 != 0) {
            return;
        }
        AppEngInternalInventory inventory = loadArcaneInventory(terminal);
        ItemStack wand = inventory.getStackInSlot(ArcaneTerminalPart.WAND_SLOT);
        if (!canDrainWith(wand)) {
            player.stopUsingItem();
            return;
        }
        if (node.drainToWand(serverLevel, player, wand, remainingUseTicks)) {
            inventory.setItemDirect(ArcaneTerminalPart.WAND_SLOT, wand);
            terminal.set(ATDataComponents.ARCANE_TERMINAL_INVENTORY.get(), inventory.toItemContainerContents());
            player.getInventory().setChanged();
        }
    }

    private static boolean canDrainWith(ItemStack wand) {
        return wand.getItem() instanceof ItemWand && SlotWorkbenchWand.isUsableWand(wand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        appendModeTooltip(this, stack, tooltip);
    }

    public static void appendModeTooltip(WirelessTerminalItem item, ItemStack stack, Consumer<Component> tooltip) {
        if (!hasDrainUpgrade(item, stack)) {
            return;
        }
        boolean draining = isDrainMode(stack);
        tooltip.accept(Component.translatable(draining
                ? "tooltip.appliedthaumaturge.wireless_arcane_terminal.drain_mode"
                : "tooltip.appliedthaumaturge.wireless_arcane_terminal.terminal_mode")
                .withStyle(draining ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
    }

    private static AppEngInternalInventory loadArcaneInventory(ItemStack terminal) {
        AppEngInternalInventory inventory = new AppEngInternalInventory(ArcaneTerminalPart.SIZE);
        inventory.fromItemContainerContents(terminal.getOrDefault(
                ATDataComponents.ARCANE_TERMINAL_INVENTORY.get(), ItemContainerContents.EMPTY));
        return inventory;
    }

    @Nullable
    private static BlockEntityNode targetedNode(Player player) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                && player.level().getBlockEntity(blockHit.getBlockPos()) instanceof BlockEntityNode node) {
            return node;
        }
        return null;
    }

    public static final class Host extends WirelessTerminalMenuHost<WirelessTerminalItem>
            implements ArcaneTerminalHost, InternalInventoryHost {
        private final UUID hostId = UUID.randomUUID();
        private final ItemStack terminalStack = getItemStack();
        private final AppEngInternalInventory arcaneInventory = new AppEngInternalInventory(this, ArcaneTerminalPart.SIZE);

        public Host(WirelessTerminalItem item, Player player, ItemMenuHostLocator locator) {
            super(item, player, locator, (p, subMenu) -> appeng.menu.MenuOpener.open(ATMenus.WIRELESS_ARCANE_TERMINAL, p, locator, true));
            arcaneInventory.fromItemContainerContents(getItemStack().getOrDefault(
                    ATDataComponents.ARCANE_TERMINAL_INVENTORY.get(), ItemContainerContents.EMPTY));
        }

        @Override
        public boolean isValid() {
            return super.isValid() && !isDrainMode(getItemStack())
                    && (isClientSide() || terminalStack == null || getItemStack() == terminalStack);
        }

        @Override
        public String getCloseHotkey() {
            return ATTerminalHotkeys.WIRELESS_ARCANE_TERMINAL;
        }

        @Override
        public AppEngInternalInventory getArcaneInventory() {
            return arcaneInventory;
        }

        @Override
        public ArcaneWorkbenchContext getArcaneContext(ServerPlayer player) {
            return ArcaneWorkbenchContext.placed(player, getAuraPosition(), hostId, player.getUUID());
        }

        public boolean matchesContext(ArcaneWorkbenchContext context) {
            return hostId.equals(context.hostIdentity()) && getAuraPosition().equals(context.position()) && isValid();
        }

        @Override
        public BlockPos getAuraPosition() {
            return getPlayer().blockPosition();
        }

        @Override
        public boolean hasChargingCard() {
            return getUpgrades().isInstalled(ATItems.UPGRADE_ARCANE);
        }

        @Override
        public void saveArcaneInventory() {
            if (!isClientSide() && isValid()) {
                getItemStack().set(ATDataComponents.ARCANE_TERMINAL_INVENTORY.get(), arcaneInventory.toItemContainerContents());
                getPlayer().getInventory().setChanged();
            }
        }

        @Override
        public void saveChangedInventory(AppEngInternalInventory inventory) {
            saveArcaneInventory();
        }

        @Override
        public void onChangeInventory(AppEngInternalInventory inventory, int slot) {
        }
    }
}
