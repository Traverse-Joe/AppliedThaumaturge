package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ArcaneInscriberPart extends AbstractTerminalPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppliedThaumaturge.id("part/arcane_inscriber_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppliedThaumaturge.id("part/arcane_inscriber_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 9, 1);
    private final AppEngInternalInventory core = new AppEngInternalInventory(this, 1, 1);
    private UUID hostId = UUID.randomUUID();
    private boolean substitute;

    public ArcaneInscriberPart(IPartItem<?> partItem) {
        super(partItem);
    }

    public AppEngInternalInventory getPattern() {
        return pattern;
    }

    public AppEngInternalInventory getCore() {
        return core;
    }

    public UUID getHostId() {
        return hostId;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
        getHost().markForSave();
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        ItemStack stack = core.getStackInSlot(0);
        if (!stack.isEmpty()) {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        pattern.clear();
        core.clear();
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        pattern.readFromNBT(data, "pattern", registries);
        core.readFromNBT(data, "core", registries);
        substitute = data.getBoolean("substitute");
        if (data.contains("inscriberHostId")) {
            try {
                hostId = UUID.fromString(data.getString("inscriberHostId"));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        pattern.writeToNBT(data, "pattern", registries);
        core.writeToNBT(data, "core", registries);
        data.putBoolean("substitute", substitute);
        data.putString("inscriberHostId", hostId.toString());
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ATMenus.ARCANE_INSCRIBER;
    }
}
