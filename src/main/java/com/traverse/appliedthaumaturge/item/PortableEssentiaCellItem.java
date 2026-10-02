package com.traverse.appliedthaumaturge.item;

import appeng.items.contents.PortableCellMenuHost;
import appeng.items.storage.StorageTier;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.locator.ItemMenuHostLocator;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PortableEssentiaCellItem extends PortableCellItem {
    public PortableEssentiaCellItem(StorageTier tier, Properties properties) {
        super(EssentiaKeyType.TYPE, ATItems.CELL_TYPES, ATMenus.PORTABLE_ESSENTIA_CELL, tier,
                properties.stacksTo(1), 0x80CAFF);
    }

    @Override
    public Identifier getRecipeId() {
        return AppliedThaumaturge.id("cells/" + getRegistryName().getPath());
    }

    @Override
    public Host getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new Host(this, player, locator);
    }

    public static final class Host extends PortableCellMenuHost<PortableEssentiaCellItem> {
        private Host(PortableEssentiaCellItem item, Player player, ItemMenuHostLocator locator) {
            super(item, player, locator, (p, subMenu) -> item.openFromInventory(p, locator, true));
        }
    }
}
