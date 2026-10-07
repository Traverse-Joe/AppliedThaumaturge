package com.traverse.appliedthaumaturge.mixin;

import appeng.client.gui.me.search.RepoSearch;
import appeng.menu.me.common.GridInventoryEntry;
import com.traverse.appliedthaumaturge.client.AspectSearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RepoSearch.class, remap = false)
public abstract class RepoSearchMixin {
    @Shadow
    private List<Predicate<GridInventoryEntry>> getPredicates(String query) {
        throw new AssertionError();
    }

    @Inject(method = "getPredicates", at = @At("HEAD"), cancellable = true)
    private void appliedthaumaturge$aspectTerms(String query, CallbackInfoReturnable<List<Predicate<GridInventoryEntry>>> cir) {
        if (!query.contains(AspectSearch.PREFIX) || !AspectSearch.isActive()) {
            return;
        }
        List<String> otherTerms = new ArrayList<>();
        List<Predicate<GridInventoryEntry>> aspectPredicates = new ArrayList<>();
        for (String term : query.toLowerCase(Locale.ROOT).trim().split("\\s+")) {
            if (term.startsWith(AspectSearch.PREFIX) && term.length() > AspectSearch.PREFIX.length()) {
                aspectPredicates.add(AspectSearch.predicate(term.substring(AspectSearch.PREFIX.length())));
            } else if (!term.isEmpty()) {
                otherTerms.add(term);
            }
        }
        if (aspectPredicates.isEmpty()) {
            return;
        }
        List<Predicate<GridInventoryEntry>> predicates = new ArrayList<>();
        if (!otherTerms.isEmpty()) {
            predicates.addAll(getPredicates(String.join(" ", otherTerms)));
        }
        predicates.addAll(aspectPredicates);
        cir.setReturnValue(predicates);
    }
}
