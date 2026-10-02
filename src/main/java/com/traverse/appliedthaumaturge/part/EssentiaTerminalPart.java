package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.util.KeyTypeSelection;
import appeng.parts.reporting.TerminalPart;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;

public class EssentiaTerminalPart extends TerminalPart {
    private final KeyTypeSelection essentiaOnly = new KeyTypeSelection(() -> {
    }, type -> type == EssentiaKeyType.TYPE);

    public EssentiaTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return essentiaOnly;
    }
}
