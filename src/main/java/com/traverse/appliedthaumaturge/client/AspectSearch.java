package com.traverse.appliedthaumaturge.client;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class AspectSearch {
    public static final String PREFIX = "%";
    public static final Identifier RESEARCH = AppliedThaumaturge.id("aspect_search");

    private AspectSearch() {
    }

    public static boolean isActive() {
        return Minecraft.getInstance().screen instanceof ArcaneTermScreen && isUnlocked();
    }

    public static boolean isUnlocked() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.level != null && KnowledgeAccess.of(minecraft.player).isResearchComplete(RESEARCH);
    }

    public static Component tooltip() {
        return Component.translatable("gui.appliedthaumaturge.search_tooltip_aspect");
    }

    public static Predicate<GridInventoryEntry> predicate(String term) {
        Set<ResourceKey<IAspect>> aspects = matchingAspects(term);
        return entry -> {
            if (aspects.isEmpty()) {
                return false;
            }
            AEKey what = entry.getWhat();
            if (what instanceof EssentiaKey essentia) {
                return aspects.contains(essentia.getAspect());
            }
            if (what instanceof AEItemKey item) {
                for (AspectInstance instance : AspectIndexAccess.of(item.toStack()).entries()) {
                    if (instance.amount() > 0 && instance.aspect().unwrapKey().filter(aspects::contains).isPresent()) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    private static Set<ResourceKey<IAspect>> matchingAspects(String term) {
        Set<ResourceKey<IAspect>> matches = new HashSet<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return matches;
        }
        String query = term.toLowerCase(Locale.ROOT);
        minecraft.level.registryAccess().lookup(IAspect.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(holder -> {
            if (!AspectKnowledgeAccess.isKnown(holder)) {
                return;
            }
            String path = holder.key().identifier().getPath();
            String name = AspectComponents.trueName(holder).getString().toLowerCase(Locale.ROOT);
            if (path.startsWith(query) || name.startsWith(query)) {
                matches.add(holder.key());
            }
        }));
        return matches;
    }
}
