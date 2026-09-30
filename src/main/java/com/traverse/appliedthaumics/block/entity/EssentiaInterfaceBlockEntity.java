package com.traverse.appliedthaumics.block.entity;

import appeng.api.config.Actionable;
import appeng.blockentity.ServerTickingBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.traverse.appliedthaumics.registry.ATBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EssentiaInterfaceBlockEntity extends EssentiaNetworkBlockEntity implements IEssentiaTransport, ServerTickingBlockEntity {
    public static final int SUCTION = 32;

    private int tick;

    public EssentiaInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ATBlockEntities.ESSENTIA_INTERFACE.get(), pos, state);
    }

    @Override
    public void serverTick() {
        if (++tick % 5 != 0 || level == null || !isNetworkActive()) {
            return;
        }
        for (Direction dir : Direction.values()) {
            IEssentiaTransport neighbour = level.getCapability(EssentiaCapabilities.TRANSPORT, worldPosition.relative(dir), dir.getOpposite());
            if (neighbour == null || neighbour instanceof EssentiaInterfaceBlockEntity || !neighbour.canOutputTo(dir.getOpposite())) {
                continue;
            }
            if (neighbour.getSuctionAmount(dir.getOpposite()) >= SUCTION || SUCTION < neighbour.getMinimumSuction()) {
                continue;
            }
            Holder<IAspect> aspect = neighbour.getEssentiaType(dir.getOpposite());
            if (aspect == null || neighbour.getEssentiaAmount(dir.getOpposite()) <= 0) {
                continue;
            }
            if (insertIntoNetwork(aspect, 1, Actionable.SIMULATE) < 1) {
                continue;
            }
            int taken = neighbour.takeEssentia(aspect, 1, dir.getOpposite());
            if (taken > 0) {
                int inserted = insertIntoNetwork(aspect, taken, Actionable.MODULATE);
                if (inserted < taken) {
                    neighbour.addEssentia(aspect, taken - inserted, dir.getOpposite());
                }
            }
        }
    }

    @Nullable
    private Holder<IAspect> requestedAspect(@Nullable Direction face) {
        if (face == null || level == null) {
            return null;
        }
        IEssentiaTransport neighbour = level.getCapability(EssentiaCapabilities.TRANSPORT, worldPosition.relative(face), face.getOpposite());
        return neighbour == null ? null : neighbour.getSuctionType(face.getOpposite());
    }

    @Nullable
    private Holder<IAspect> mostPlentiful() {
        AspectInstance best = null;
        for (AspectInstance entry : networkContents().entries()) {
            if (best == null || entry.amount() > best.amount()) {
                best = entry;
            }
        }
        return best == null ? null : best.aspect();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return true;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
    }

    @Nullable
    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return isNetworkActive() ? SUCTION : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return extractFromNetwork(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return insertIntoNetwork(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return extractFromNetwork(aspect, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return insertIntoNetwork(aspect, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        return insertIntoNetwork(aspect, Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Nullable
    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        Holder<IAspect> requested = requestedAspect(face);
        if (requested != null) {
            return networkAmount(requested) > 0 ? requested : null;
        }
        return mostPlentiful();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        Holder<IAspect> aspect = getEssentiaType(face);
        return aspect == null ? 0 : (int) Math.min(Integer.MAX_VALUE, networkAmount(aspect));
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
