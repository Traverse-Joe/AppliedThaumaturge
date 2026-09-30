package com.traverse.appliedthaumics.registry;

import com.traverse.appliedthaumics.AppliedThaumics;
import com.traverse.appliedthaumics.knowledge.KnowledgeCoreContents;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, AppliedThaumics.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KnowledgeCoreContents>> KNOWLEDGE_CORE =
            COMPONENTS.registerComponentType("knowledge_core", builder -> builder
                    .persistent(KnowledgeCoreContents.CODEC)
                    .networkSynchronized(KnowledgeCoreContents.STREAM_CODEC)
                    .cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ENTANGLEMENT =
            COMPONENTS.registerComponentType("entanglement", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    private ATDataComponents() {
    }
}
