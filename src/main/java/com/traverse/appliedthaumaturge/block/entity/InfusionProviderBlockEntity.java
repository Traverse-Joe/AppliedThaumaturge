package com.traverse.appliedthaumaturge.block.entity;

import appeng.api.config.Actionable;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.traverse.appliedthaumaturge.registry.ATBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;

public class InfusionProviderBlockEntity extends EssentiaNetworkBlockEntity implements IAspectSource {
    public InfusionProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ATBlockEntities.INFUSION_PROVIDER.get(), pos, state);
    }

    @Override
    public boolean isBlocked() {
        return !isNetworkActive();
    }

    @Override
    public AspectList getAspects() {
        return networkContents();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return false;
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        return amount;
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (extractFromNetwork(aspect, amount, Actionable.SIMULATE) < amount) {
            return false;
        }
        return extractFromNetwork(aspect, amount, Actionable.MODULATE) == amount;
    }

    @Override
    public boolean holds(Holder<IAspect> aspect, int amount) {
        return networkAmount(aspect) >= amount;
    }

    @Override
    public int amountOf(Holder<IAspect> aspect) {
        return (int) Math.min(Integer.MAX_VALUE, networkAmount(aspect));
    }
}
