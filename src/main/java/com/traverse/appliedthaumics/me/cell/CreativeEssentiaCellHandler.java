package com.traverse.appliedthaumics.me.cell;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.traverse.appliedthaumics.item.CreativeEssentiaCellItem;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public final class CreativeEssentiaCellHandler implements ICellHandler {
    public static final CreativeEssentiaCellHandler INSTANCE = new CreativeEssentiaCellHandler();

    private CreativeEssentiaCellHandler() {
    }

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof CreativeEssentiaCellItem;
    }

    @Nullable
    @Override
    public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        return isCell(is) ? new Inventory(is) : null;
    }

    private record Inventory(ItemStack stack) implements StorageCell {
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return what instanceof EssentiaKey ? amount : 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return what instanceof EssentiaKey ? amount : 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            server.registryAccess().lookup(IAspect.REGISTRY_KEY).ifPresent(lookup ->
                    lookup.listElementIds().forEach(id -> out.add(EssentiaKey.of(id), Integer.MAX_VALUE)));
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return what instanceof EssentiaKey;
        }

        @Override
        public CellState getStatus() {
            return CellState.TYPES_FULL;
        }

        @Override
        public double getIdleDrain() {
            return 0;
        }

        @Override
        public boolean canFitInsideCell() {
            return false;
        }

        @Override
        public Component getDescription() {
            return stack.getHoverName();
        }

        @Override
        public void persist() {
        }
    }
}
