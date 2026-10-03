package com.traverse.appliedthaumaturge.item;

import appeng.api.config.Actionable;
import appeng.api.storage.StorageHelper;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.DimensionalBlockPos;
import appeng.me.helpers.PlayerSource;
import appeng.menu.locator.MenuLocators;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import java.util.function.DoubleSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WirelessEssentiaTerminalItem extends WirelessTerminalItem {
    public WirelessEssentiaTerminalItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Nullable
    @Override
    public WirelessTerminalMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return isAlchemyMode(locator.locateItem(player)) ? null : new Host(this, player, locator);
    }

    public static boolean isAlchemyMode(ItemStack terminal) {
        return terminal.getOrDefault(ATDataComponents.ALCHEMY_MODE.get(), false);
    }

    private boolean hasAlchemyUpgrade(ItemStack terminal) {
        return getUpgrades(terminal).isInstalled(ATItems.UPGRADE_ALCHEMY);
    }

    @Override
    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubMenu) {
        return !isAlchemyMode(locator.locateItem(player))
                && super.openFromInventory(player, locator, returningFromSubMenu);
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

    private InteractionResult useMode(Player player, InteractionHand hand, ItemStack terminal) {
        boolean alchemy = isAlchemyMode(terminal);
        boolean upgraded = hasAlchemyUpgrade(terminal);
        if (player.isShiftKeyDown() && (upgraded || alchemy)) {
            if (!player.level().isClientSide()) {
                terminal.set(ATDataComponents.ALCHEMY_MODE.get(), !alchemy);
                player.getInventory().setChanged();
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 0.4F, alchemy ? 0.9F : 1.2F);
            }
            return InteractionResult.CONSUME;
        }
        if (alchemy) {
            if (upgraded) {
                BlockHitResult hit = targetedBlock(player);
                if (hit != null) {
                    transferJar(player, hand, hit.getBlockPos(), true);
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static void onLeftClickJar(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack terminal = player.getMainHandItem();
        if (!(terminal.getItem() instanceof WirelessEssentiaTerminalItem item)
                || !isAlchemyMode(terminal) || !item.hasAlchemyUpgrade(terminal)
                || !(player.level().getBlockEntity(event.getPos()) instanceof BlockEntityJar)) {
            return;
        }
        event.setCanceled(true);
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
            item.transferJar(player, InteractionHand.MAIN_HAND, event.getPos(), false);
        }
    }

    @Nullable
    private static BlockHitResult targetedBlock(Player player) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit : null;
    }

    private void transferJar(Player player, InteractionHand hand, BlockPos pos, boolean fill) {
        if (player.level().isClientSide() || player.isSpectator()) {
            return;
        }
        ItemStack terminal = player.getItemInHand(hand);
        BlockHitResult hit = targetedBlock(player);
        if (!terminal.is(this) || !isAlchemyMode(terminal) || !hasAlchemyUpgrade(terminal)
                || hit == null || !hit.getBlockPos().equals(pos)
                || !Platform.hasPermissions(new DimensionalBlockPos(player.level(), pos), player)
                || !(player.level().getBlockEntity(pos) instanceof BlockEntityJar jar)) {
            return;
        }
        var aspectKey = fill ? jar.aspectFilterKey() : jar.aspectKey();
        int amount = fill ? jar.capacity() - jar.amount() : jar.amount();
        if (aspectKey == null || amount <= 0) {
            return;
        }
        EssentiaKey key = EssentiaKey.of(aspectKey);
        var aspect = key.holderOrNull(player.level().registryAccess());
        if (aspect == null) {
            return;
        }
        Host host = new Host(this, player, MenuLocators.forHand(player, hand));
        if (!host.getLinkStatus().connected()) {
            return;
        }
        var storage = jar.storage(Direction.UP);
        int possible;
        try (Transaction tx = Transaction.openRoot()) {
            possible = fill ? storage.insert(aspect, amount, tx) : storage.extract(aspect, amount, tx);
        }
        if (possible <= 0 || !host.consumeIdlePower(Actionable.MODULATE)) {
            return;
        }
        var inventory = host.getInventory();
        var source = new PlayerSource(player, host);
        long moved = fill
                ? StorageHelper.poweredExtraction(host, inventory, key, possible, source)
                : StorageHelper.poweredInsert(host, inventory, key, possible, source);
        if (moved <= 0) {
            return;
        }
        boolean committed = false;
        try (Transaction tx = Transaction.openRoot()) {
            int transferred = fill ? storage.insert(aspect, (int) moved, tx) : storage.extract(aspect, (int) moved, tx);
            if (transferred == moved) {
                tx.commit();
                committed = true;
            }
        }
        if (!committed) {
            if (fill) {
                inventory.insert(key, moved, Actionable.MODULATE, source);
            } else {
                inventory.extract(key, moved, Actionable.MODULATE, source);
            }
        } else {
            player.level().playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (hasAlchemyUpgrade(stack)) {
            boolean alchemy = isAlchemyMode(stack);
            tooltip.accept(Component.translatable(alchemy
                    ? "tooltip.appliedthaumaturge.wireless_essentia_terminal.alchemy_mode"
                    : "tooltip.appliedthaumaturge.wireless_essentia_terminal.terminal_mode")
                    .withStyle(alchemy ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
        }
    }

    private static final class Host extends WirelessTerminalMenuHost<WirelessEssentiaTerminalItem> {
        private final KeyTypeSelection essentiaOnly = new KeyTypeSelection(() -> {
        }, type -> type == EssentiaKeyType.TYPE);

        private Host(WirelessEssentiaTerminalItem item, Player player, ItemMenuHostLocator locator) {
            super(item, player, locator, (p, subMenu) -> item.openFromInventory(p, locator, true));
        }

        @Override
        public boolean isValid() {
            return super.isValid() && !isAlchemyMode(getItemStack());
        }

        @Override
        public KeyTypeSelection getKeyTypeSelection() {
            return essentiaOnly;
        }
    }
}
