package com.traverse.appliedthaumics.registry;

import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.traverse.appliedthaumics.AppliedThaumics;
import com.traverse.appliedthaumics.wand.EntangledRodUpdate;
import com.traverse.appliedthaumics.wand.EntangledWands;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATWandParts {
    public static final DeferredRegister<WandRod> RODS = DeferredRegister.create(WandRod.REGISTRY_KEY, AppliedThaumics.MODID);

    public static final DeferredHolder<WandRod, WandRod> ENTANGLED = RODS.register("entangled", () -> new WandRod(
            100, 9, AppliedThaumics.id("textures/models/wand_rod_entangled.png"), new EntangledRodUpdate(),
            true, false, false, AppliedThaumics.id("entangled_wand_core"), EntangledWands.INSTANCE, EntangledWands.INSTANCE));

    private ATWandParts() {
    }
}
