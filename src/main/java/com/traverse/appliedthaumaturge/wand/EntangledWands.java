package com.traverse.appliedthaumaturge.wand;

import com.leclowndu93150.thaumaturge.api.infusion.InfusionCraftedEvent;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnAssemble;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.traverse.appliedthaumaturge.registry.ATDataComponents;
import com.traverse.appliedthaumaturge.registry.ATItems;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public final class EntangledWands implements IWandVisStorage, IWandRodOnAssemble {
    public static final EntangledWands INSTANCE = new EntangledWands();

    private EntangledWands() {
    }

    @Nullable
    private static Long linkId(ItemStack stack) {
        return stack.get(ATDataComponents.ENTANGLEMENT.get());
    }

    @Nullable
    private static EntangledVisData serverData() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || !server.isSameThread()) {
            return null;
        }
        return EntangledVisData.get(server);
    }

    private static WandVis ownVis(ItemStack stack) {
        WandVis vis = stack.get(TTDataComponents.WAND_VIS.get());
        return vis != null ? vis : WandVis.EMPTY;
    }

    @Override
    public WandVis getVis(ItemStack wand) {
        Long id = linkId(wand);
        EntangledVisData data = id == null ? null : serverData();
        if (data == null) {
            return ownVis(wand);
        }
        WandVis pool = data.pool(id);
        if (pool == null) {
            pool = ownVis(wand);
            data.setPool(id, pool);
        }
        return pool;
    }

    @Override
    public void setVis(ItemStack wand, WandVis vis) {
        Long id = linkId(wand);
        EntangledVisData data = id == null ? null : serverData();
        if (data != null) {
            data.setPool(id, vis);
        }
        wand.set(TTDataComponents.WAND_VIS.get(), vis);
    }

    public static boolean mirror(ItemStack wand) {
        Long id = linkId(wand);
        EntangledVisData data = id == null ? null : serverData();
        WandVis pool = data == null ? null : data.pool(id);
        if (pool != null && !pool.equals(ownVis(wand))) {
            wand.set(TTDataComponents.WAND_VIS.get(), pool);
            return true;
        }
        return false;
    }

    @Override
    public void onAssemble(ItemStack wand, IArcaneCraftingInput input) {
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                ItemStack ingredient = input.getItem(x, y);
                Long id = linkId(ingredient);
                if (id != null && ingredient.is(ATItems.ENTANGLED_WAND_CORE.get())) {
                    wand.set(ATDataComponents.ENTANGLEMENT.get(), id);
                    return;
                }
            }
        }
    }

    public static void onInfusionCrafted(InfusionCraftedEvent event) {
        ItemStack result = event.getResult();
        if (!result.is(ATItems.ENTANGLED_WAND_CORE.get()) || result.has(ATDataComponents.ENTANGLEMENT.get())) {
            return;
        }
        long id = ThreadLocalRandom.current().nextLong();
        ItemStack paired = result.copy();
        paired.set(ATDataComponents.ENTANGLEMENT.get(), id == 0 ? 1 : id);
        event.setResult(paired);
    }
}
