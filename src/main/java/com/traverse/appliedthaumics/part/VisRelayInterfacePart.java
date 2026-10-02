package com.traverse.appliedthaumics.part;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.items.tools.MemoryCardItem;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.traverse.appliedthaumics.vis.VisRelayLink;
import java.util.UUID;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class VisRelayInterfacePart extends AEBasePart implements IVisRelaySource {
    private static final double POWER_PER_REQUEST = 4;
    private String sourceId = UUID.randomUUID().toString();
    @Nullable
    private VisRelayLink link;
    private boolean visiting;
    private static final Map<IEnergyService, PowerJournal> POWER_JOURNALS = new WeakHashMap<>();

    public VisRelayInterfacePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(0);
    }

    public String getSourceId() {
        return sourceId;
    }

    public boolean isOutput() {
        return link != null;
    }

    @Nullable
    private IVisRelaySource upstream() {
        if (!(getLevel() instanceof ServerLevel level) || !getMainNode().isActive()) {
            return null;
        }
        if (link != null) {
            return link.resolve(this);
        }
        var position = getBlockEntity().getBlockPos().relative(getSide());
        if (!level.hasChunkAt(position) || !(level.getBlockEntity(position) instanceof BlockEntityVisRelay relay)) {
            return null;
        }
        var linked = relay.resolveSource(level);
        return linked == null ? null : linked.source();
    }

    @Override
    public boolean canSupply() {
        if (visiting) {
            return false;
        }
        visiting = true;
        try {
            IVisRelaySource source = upstream();
            return source != null && source.canSupply();
        } finally {
            visiting = false;
        }
    }

    @Override
    public int availableCentivis(ResourceKey<IAspect> aspect) {
        if (visiting) {
            return 0;
        }
        visiting = true;
        try {
            IVisRelaySource source = upstream();
            if (source == null || !source.canSupply() || (!isOutput() && !power().canReserve())) {
                return 0;
            }
            return Math.max(0, source.availableCentivis(aspect));
        } finally {
            visiting = false;
        }
    }

    @Override
    public int drainCentivis(ResourceKey<IAspect> aspect, int amount, TransactionContext transaction) {
        if (amount <= 0 || visiting) {
            return 0;
        }
        visiting = true;
        try {
            IVisRelaySource source = upstream();
            if (source == null || !source.canSupply() || (!isOutput() && !power().canReserve())) {
                return 0;
            }
            int drained = Math.clamp(source.drainCentivis(aspect, amount, transaction), 0, amount);
            if (drained > 0 && !isOutput()) {
                power().reserve(transaction);
            }
            return drained;
        } finally {
            visiting = false;
        }
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onUseItemOn(ItemStack stack, Player player, InteractionHand hand, Vec3 hit) {
        if (!(stack.getItem() instanceof IMemoryCard card)) {
            return super.onUseItemOn(stack, player, hand, hit);
        }
        if (isClientSide()) {
            return true;
        }
        if (!Platform.hasPermissions(getHost().getLocation(), player)) {
            return false;
        }
        if (InteractionUtil.isInAlternateUseMode(player)) {
            link = null;
            MemoryCardItem.clearCard(stack);
            stack.set(AEComponents.EXPORTED_SETTINGS, VisRelayLink.of(this).cardSettings());
            stack.set(AEComponents.EXPORTED_SETTINGS_SOURCE, getName());
            card.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
        } else {
            VisRelayLink saved = VisRelayLink.fromCard(stack);
            if (saved == null) {
                if (stack.has(AEComponents.EXPORTED_SETTINGS_SOURCE)) {
                    card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    return true;
                }
                link = null;
                card.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
            } else if (saved.resolve(this) == null) {
                card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                return true;
            } else {
                link = saved;
                card.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            }
        }
        getHost().markForSave();
        getHost().markForUpdate();
        getLevel().invalidateCapabilities(getBlockEntity().getBlockPos());
        return true;
    }

    @Override
    public void readFromNBT(ValueInput input) {
        super.readFromNBT(input);
        sourceId = input.getStringOr("visSourceId", sourceId);
        link = input.read("visSource", VisRelayLink.CODEC).orElse(null);
    }

    @Override
    public void writeToNBT(ValueOutput output) {
        super.writeToNBT(output);
        output.putString("visSourceId", sourceId);
        if (link != null) {
            output.store("visSource", VisRelayLink.CODEC, link);
        }
    }

    @Override
    public void getBoxes(IPartCollisionHelper helper) {
        helper.addBox(5, 5, 12, 11, 11, 13);
        helper.addBox(3, 3, 13, 13, 13, 14);
        helper.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 2;
    }

    private PowerJournal power() {
        var energy = getMainNode().getGrid().getEnergyService();
        return POWER_JOURNALS.computeIfAbsent(energy, PowerJournal::new);
    }

    private static final class PowerJournal extends SnapshotJournal<Double> {
        private double reserved;
        private final WeakReference<IEnergyService> energy;

        PowerJournal(IEnergyService energy) {
            this.energy = new WeakReference<>(energy);
        }

        boolean canReserve() {
            var service = energy.get();
            return service != null && service.extractAEPower(reserved + POWER_PER_REQUEST,
                    Actionable.SIMULATE, PowerMultiplier.CONFIG) + 0.0001 >= reserved + POWER_PER_REQUEST;
        }

        void reserve(TransactionContext transaction) {
            updateSnapshots(transaction);
            reserved += POWER_PER_REQUEST;
        }

        @Override
        protected Double createSnapshot() {
            return reserved;
        }

        @Override
        protected void revertToSnapshot(Double snapshot) {
            reserved = snapshot;
        }

        @Override
        protected void onRootCommit(Double original) {
            double cost = reserved - original;
            reserved = original;
            var service = energy.get();
            if (service != null) {
                service.extractAEPower(cost, Actionable.MODULATE, PowerMultiplier.CONFIG);
            }
        }
    }
}
