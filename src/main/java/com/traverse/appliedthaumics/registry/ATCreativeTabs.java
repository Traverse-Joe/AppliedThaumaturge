package com.traverse.appliedthaumics.registry;

import com.traverse.appliedthaumics.AppliedThaumics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ATCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedThaumics.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.appliedthaumics"))
            .icon(() -> ATItems.ESSENTIA_CELL_1K.get().getDefaultInstance())
            .displayItems((params, output) -> ATItems.ITEMS.getEntries().stream()
                    .filter(entry -> entry != ATItems.KNOWLEDGE_CORE)
                    .forEach(entry -> output.accept(entry.get())))
            .build());

    private ATCreativeTabs() {
    }
}
