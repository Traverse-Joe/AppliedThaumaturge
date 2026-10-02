package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ArcaneInscriberPart extends AbstractTerminalPart {
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
    public void readFromNBT(ValueInput input) {
        super.readFromNBT(input);
        pattern.readFromNBT(input, "pattern");
        core.readFromNBT(input, "core");
        substitute = input.getBooleanOr("substitute", false);
        input.getString("inscriberHostId").ifPresent(id -> {
            try {
                hostId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
        });
    }

    @Override
    public void writeToNBT(ValueOutput output) {
        super.writeToNBT(output);
        pattern.writeToNBT(output, "pattern");
        core.writeToNBT(output, "core");
        output.putBoolean("substitute", substitute);
        output.putString("inscriberHostId", hostId.toString());
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ATMenus.ARCANE_INSCRIBER;
    }
}
