package com.traverse.appliedthaumaturge.vis;

import appeng.api.parts.IPartHost;
import appeng.core.definitions.AEBlockEntities;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.traverse.appliedthaumaturge.part.VisRelayInterfacePart;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record VisRelayHostSource(IPartHost host) implements IVisRelaySource {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(VisRelayCapabilities.SOURCE, AEBlockEntities.CABLE_BUS.get(),
                (host, context) -> new VisRelayHostSource(host));
    }

    @Override
    public boolean canSupply() {
        for (Direction side : Direction.values()) {
            if (host.getPart(side) instanceof VisRelayInterfacePart part && part.isOutput() && part.canSupply()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int availableCentivis(ResourceKey<IAspect> aspect) {
        int available = 0;
        for (Direction side : Direction.values()) {
            if (host.getPart(side) instanceof VisRelayInterfacePart part && part.isOutput()) {
                available = Math.max(available, part.availableCentivis(aspect));
            }
        }
        return available;
    }

    @Override
    public int drainCentivis(ResourceKey<IAspect> aspect, int amount, TransactionContext transaction) {
        int remaining = Math.max(0, amount);
        for (Direction side : Direction.values()) {
            if (remaining > 0 && host.getPart(side) instanceof VisRelayInterfacePart part && part.isOutput()) {
                remaining -= part.drainCentivis(aspect, remaining, transaction);
            }
        }
        return Math.max(0, amount) - remaining;
    }
}
