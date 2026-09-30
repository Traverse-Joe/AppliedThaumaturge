package com.traverse.appliedthaumics.me.strategy;

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
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class EssentiaContainerItemStrategy implements ContainerItemStrategy<EssentiaKey, EssentiaContainerItemStrategy.Context> {

    public record Context(ItemAccess access, Player player, @Nullable AbstractContainerMenu menu) {
        @Nullable
        IEssentiaItemStorage storage() {
            ItemResource resource = access.getResource();
            if (resource.isEmpty()) {
                return null;
            }
            return resource.toStack(1).getCapability(EssentiaCapabilities.ITEM_STORAGE);
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
        return wrap(ItemAccess.forPlayerCursor(player, menu), player, menu);
    }

    @Nullable
    @Override
    public Context findPlayerSlotContext(Player player, int slot) {
        return wrap(ItemAccess.forPlayerSlot(player, slot), player, player.containerMenu);
    }

    @Nullable
    private static Context wrap(ItemAccess access, Player player, @Nullable AbstractContainerMenu menu) {
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
        ItemStack resulting = result.resultingStack();
        ItemResource newResource = resulting.isEmpty() ? null : ItemResource.of(resulting);
        try (Transaction tx = Transaction.openRoot()) {
            int changed;
            if (newResource == null) {
                changed = context.access().extract(context.access().getResource(), 1, tx);
            } else {
                changed = context.access().exchange(newResource, 1, tx);
            }
            if (changed != 1) {
                return 0;
            }
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
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
