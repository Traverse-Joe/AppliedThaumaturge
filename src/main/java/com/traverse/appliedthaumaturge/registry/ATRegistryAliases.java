package com.traverse.appliedthaumaturge.registry;

import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ATRegistryAliases {
    private static final String LEGACY_NAMESPACE = "appliedthaumics";

    private ATRegistryAliases() {
    }

    public static void register(RegisterEvent event) {
        Registry<?> registry = event.getRegistry();
        for (ResourceLocation id : registry.keySet()) {
            if (id.getNamespace().equals(AppliedThaumaturge.MODID)) {
                registry.addAlias(ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, id.getPath()), id);
            }
        }
    }
}
