package com.traverse.appliedthaumaturge.me.strategy;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.menu.me.common.MEStorageMenu;
import appeng.api.stacks.GenericStack;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.leclowndu93150.thaumaturge.api.essentia.ItemEssentiaTransferResult;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EssentiaContainerItemStrategy implements ContainerItemStrategy<EssentiaKey, EssentiaContainerItemStrategy.Context> {

    public record Context(EssentiaItemAccess access, Player player, @Nullable AbstractContainerMenu menu) {
        @Nullable
        IEssentiaItemStorage storage() {
            ItemStack stack = access.getOne();
            if (stack.isEmpty()) {
                return null;
            }
            return stack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        }

        HolderLookup.Provider registries() {
            return player.level().registryAccess();
        }

        long networkAvailable(EssentiaKey key, long amount) {
            if (menu instanceof MEStorageMenu storageMenu) {
                return storageMenu.getHost().getInventory().extract(key, amount, Actionable.SIMULATE, IActionSource.ofPlayer(player));
            }
            return amount;
        }
    }

    @Nullable
    @Override
    public GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        IEssentiaItemStorage storage = stack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        return storage == null ? null : first(storage);
    }

    @Nullable
    @Override
    public Context findCarriedContext(Player player, AbstractContainerMenu menu) {
        return wrap(EssentiaItemAccess.forPlayerCursor(player, menu), player, menu);
    }

    @Nullable
    @Override
    public Context findPlayerSlotContext(Player player, int slot) {
        return wrap(EssentiaItemAccess.forPlayerSlot(player, slot), player, player.containerMenu);
    }

    @Nullable
    private static Context wrap(EssentiaItemAccess access, Player player, @Nullable AbstractContainerMenu menu) {
        Context context = new Context(access, player, menu);
        return context.storage() == null ? null : context;
    }

    @Override
    public long extract(Context context, EssentiaKey what, long amount, Actionable mode) {
        IEssentiaItemStorage storage = context.storage();
        Holder<IAspect> aspect = what.holderOrNull(context.registries());
        if (storage == null || aspect == null) {
            return 0;
        }
        ItemEssentiaTransferResult result = storage.extract(aspect, EssentiaExternalStorageStrategy.clamp(amount));
        if (result.amountMoved() == 0) {
            int stored = storage.contents().amountOf(aspect);
            if (stored > amount) {
                result = storage.extract(aspect, stored);
            }
        }
        return apply(context, result, mode);
    }

    @Override
    public long insert(Context context, EssentiaKey what, long amount, Actionable mode) {
        IEssentiaItemStorage storage = context.storage();
        Holder<IAspect> aspect = what.holderOrNull(context.registries());
        if (storage == null || aspect == null || !storage.canInsert(aspect)) {
            return 0;
        }
        ItemEssentiaTransferResult result = storage.insert(aspect, EssentiaExternalStorageStrategy.clamp(amount));
        if (result.amountMoved() == 0) {
            int space = storage.capacity(aspect) - storage.contents().totalAmount();
            if (space > amount && context.networkAvailable(what, space) >= space) {
                result = storage.insert(aspect, space);
            }
        }
        return apply(context, result, mode);
    }

    private static long apply(Context context, ItemEssentiaTransferResult result, Actionable mode) {
        if (result.amountMoved() <= 0) {
            return 0;
        }
        if (!context.access().exchange(result.resultingStack(), mode == Actionable.SIMULATE)) {
            return 0;
        }
        return result.amountMoved();
    }

    @Override
    public void playFillSound(Player player, EssentiaKey what) {
    }

    @Override
    public void playEmptySound(Player player, EssentiaKey what) {
    }

    @Nullable
    @Override
    public GenericStack getExtractableContent(Context context) {
        IEssentiaItemStorage storage = context.storage();
        return storage == null ? null : first(storage);
    }

    @Nullable
    private static GenericStack first(IEssentiaItemStorage storage) {
        for (AspectInstance entry : storage.contents().entries()) {
            EssentiaKey key = EssentiaKey.of(entry.aspect());
            if (key != null && entry.amount() > 0) {
                return new GenericStack(key, entry.amount());
            }
        }
        return null;
    }
}
