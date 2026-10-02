package com.traverse.appliedthaumaturge.arcane;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public final class ArcaneCrafter {
    private ArcaneCrafter() {
    }

    public static ArcaneCraftingInput.Positioned prepare(InternalInventory inv, ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>(ArcaneTerminalPart.SIZE);
        for (int i = 0; i < ArcaneTerminalPart.SIZE; i++) {
            items.add(inv.getStackInSlot(i).copy());
        }
        ArcaneCraftingInput.Positioned positioned = ArcaneCraftingInput.ofPositioned(3, 3, items);
        positioned.input().withPlayer(player);
        return positioned;
    }

    public static ArcaneWorkbenchContext context(ServerPlayer player, ArcaneTerminalPart part) {
        BlockPos pos = part.getHost().getBlockEntity().getBlockPos();
        return ArcaneWorkbenchContext.placed(player, pos, part.getHostId(), null);
    }

    @Nullable
    public static ArcaneCraftingTransaction.Inspection inspect(ServerPlayer player, ArcaneTerminalPart part, InternalInventory inv) {
        ArcaneCraftingInput.Positioned positioned = prepare(inv, player);
        if (positioned.input().isEmpty()) {
            return null;
        }
        return ArcaneCraftingTransaction.inspect(context(player, part), player, positioned.input());
    }

    @Nullable
    public static ArcaneCraftingTransaction.Result previewCost(ServerPlayer player, ArcaneTerminalPart part, InternalInventory inv) {
        ArcaneCraftingInput.Positioned positioned = prepare(inv, player);
        if (positioned.input().isEmpty()) {
            return null;
        }
        return ArcaneCraftingTransaction.preview(context(player, part), player, positioned.input());
    }

    public static ItemStack craft(ServerPlayer player, ArcaneTerminalPart part, InternalInventory inv,
                                  @Nullable MEStorage storage, IEnergySource energy, IActionSource source) {
        ArcaneCraftingInput.Positioned positioned = prepare(inv, player);
        if (positioned.input().isEmpty()) {
            return ItemStack.EMPTY;
        }
        IArcaneCraftingStore.Consumption[] captured = new IArcaneCraftingStore.Consumption[1];
        ArcaneCraftingTransaction.Result result;
        try (Transaction tx = Transaction.openRoot()) {
            result = ArcaneCraftingTransaction.craft(context(player, part), player, positioned.input(), (consumption, transaction) -> {
                captured[0] = consumption;
                return true;
            }, tx);
            if (!result.successful() || captured[0] == null) {
                return ItemStack.EMPTY;
            }
            tx.commit();
        }
        apply(captured[0], positioned, inv, player, storage, energy, source);
        return result.output();
    }

    private static void apply(IArcaneCraftingStore.Consumption consumption, ArcaneCraftingInput.Positioned positioned, InternalInventory inv,
                              ServerPlayer player, @Nullable MEStorage storage, IEnergySource energy, IActionSource source) {
        int width = positioned.input().width();
        List<ItemStack> remainders = consumption.remainders();
        for (int i = 0; i < consumption.grid().size(); i++) {
            int slot = (i % width + positioned.left()) + (i / width + positioned.top()) * 3;
            ItemStack original = inv.getStackInSlot(slot);
            if (original.isEmpty()) {
                continue;
            }
            ItemStack template = original.copyWithCount(1);
            ItemStack shrunk = original.copy();
            shrunk.shrink(1);
            ItemStack remainder = i < remainders.size() ? remainders.get(i) : ItemStack.EMPTY;
            if (shrunk.isEmpty()) {
                if (!remainder.isEmpty()) {
                    shrunk = remainder;
                    remainder = ItemStack.EMPTY;
                } else if (storage != null) {
                    AEItemKey key = AEItemKey.of(template);
                    if (StorageHelper.poweredExtraction(energy, storage, key, 1, source, Actionable.MODULATE) > 0) {
                        shrunk = template;
                    }
                }
            }
            inv.setItemDirect(slot, shrunk);
            if (!remainder.isEmpty()) {
                giveBack(remainder, player, storage, source);
            }
        }

        for (AspectInstance needed : consumption.crystals().entries()) {
            int remaining = needed.amount();
            for (int slot = ArcaneTerminalPart.CRYSTAL_START; slot < ArcaneTerminalPart.CRYSTAL_START + ArcaneTerminalPart.CRYSTAL_SLOTS && remaining > 0; slot++) {
                ItemStack crystal = inv.getStackInSlot(slot);
                Holder<IAspect> aspect = crystal.isEmpty() ? null : ItemEssentiaCrystal.aspectOf(crystal);
                if (aspect == null || !aspect.equals(needed.aspect())) {
                    continue;
                }
                int take = Math.min(remaining, crystal.getCount());
                ItemStack left = crystal.copy();
                left.shrink(take);
                remaining -= take;
                if (storage != null) {
                    AEItemKey key = AEItemKey.of(crystal.copyWithCount(1));
                    long refilled = StorageHelper.poweredExtraction(energy, storage, key, take, source, Actionable.MODULATE);
                    if (refilled > 0) {
                        left = crystal.copyWithCount(left.getCount() + (int) refilled);
                    }
                }
                inv.setItemDirect(slot, left);
            }
        }

        if (!inv.getStackInSlot(ArcaneTerminalPart.WAND_SLOT).isEmpty()) {
            inv.setItemDirect(ArcaneTerminalPart.WAND_SLOT, consumption.wand());
        }
    }

    private static void giveBack(ItemStack stack, ServerPlayer player, @Nullable MEStorage storage, IActionSource source) {
        if (storage != null) {
            long inserted = storage.insert(AEItemKey.of(stack), stack.getCount(), Actionable.MODULATE, source);
            stack = stack.copyWithCount(stack.getCount() - (int) inserted);
        }
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
