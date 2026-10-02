package com.traverse.appliedthaumaturge.part;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;

final class EssentiaTransferContext implements StackTransferContext {
    private final IStorageService storage;
    private final IEnergySource energy;
    private final IActionSource source;
    private final IPartitionList filter;
    private final int initialOperations;
    private int operations;
    private boolean inverted;

    EssentiaTransferContext(IStorageService storage, IEnergySource energy, IActionSource source, int operations, IPartitionList filter) {
        this.storage = storage;
        this.energy = energy;
        this.source = source;
        this.filter = filter;
        this.initialOperations = operations;
        this.operations = operations;
    }

    @Override
    public IStorageService getInternalStorage() {
        return storage;
    }

    @Override
    public IEnergySource getEnergySource() {
        return energy;
    }

    @Override
    public IActionSource getActionSource() {
        return source;
    }

    @Override
    public int getOperationsRemaining() {
        return operations;
    }

    @Override
    public void setOperationsRemaining(int operationsRemaining) {
        operations = operationsRemaining;
    }

    @Override
    public boolean hasOperationsLeft() {
        return operations > 0;
    }

    @Override
    public boolean hasDoneWork() {
        return initialOperations > operations;
    }

    @Override
    public boolean isKeyTypeEnabled(AEKeyType type) {
        return true;
    }

    @Override
    public boolean isInFilter(AEKey key) {
        return filter.isEmpty() || filter.isListed(key);
    }

    @Override
    public IPartitionList getFilter() {
        return filter;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public boolean isInverted() {
        return !filter.isEmpty() && inverted;
    }

    @Override
    public boolean canInsert(AEItemKey what, long amount) {
        return storage.getInventory().insert(what, amount, Actionable.SIMULATE, source) > 0;
    }

    @Override
    public void reduceOperationsRemaining(long amount) {
        operations = (int) (operations - amount);
    }
}
