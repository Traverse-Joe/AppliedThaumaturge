package com.traverse.appliedthaumaturge.registry;

import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.wand.EntangledRodUpdate;
import com.traverse.appliedthaumaturge.wand.EntangledWands;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATWandParts {
    public static final DeferredRegister<WandRod> RODS = DeferredRegister.create(WandRod.REGISTRY_KEY, AppliedThaumaturge.MODID);

    public static final DeferredHolder<WandRod, WandRod> ENTANGLED = RODS.register("entangled", () -> new WandRod(
            100, 9, AppliedThaumaturge.id("textures/models/wand_rod_entangled.png"), new EntangledRodUpdate(),
            true, false, false, AppliedThaumaturge.id("entangled_wand_core"), EntangledWands.INSTANCE, EntangledWands.INSTANCE));

    private ATWandParts() {
    }
}
