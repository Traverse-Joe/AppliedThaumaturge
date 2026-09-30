package com.traverse.appliedthaumics.client;

import appeng.api.config.ActionItems;
import appeng.util.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.traverse.appliedthaumics.menu.ArcaneInscriberMenu;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneInscriberScreen extends MEStorageScreen<ArcaneInscriberMenu> {
    private final ActionButton encodeButton;
    private final ActionButton eraseButton;
    private final ToggleButton substitutionsButton;

    public ArcaneInscriberScreen(ArcaneInscriberMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        encodeButton = new ActionButton(ActionItems.ENCODE, act -> menu.encode());
        widgets.add("encode", encodeButton);
        eraseButton = new ActionButton(ActionItems.CLOSE, act -> menu.erase());
        widgets.add("erase", eraseButton);

        ActionButton clearButton = new ActionButton(ActionItems.S_CLOSE, act -> menu.clear());
        clearButton.setHalfSize(true);
        clearButton.setDisableBackground(true);
        widgets.add("clearPattern", clearButton);

        substitutionsButton = new ToggleButton(Icon.S_SUBSTITUTION_ENABLED, Icon.S_SUBSTITUTION_DISABLED, menu::setSubstitute);
        substitutionsButton.setHalfSize(true);
        substitutionsButton.setDisableBackground(true);
        substitutionsButton.setTooltipOn(List.of(ButtonToolTips.SubstitutionsOn.text(), ButtonToolTips.SubstitutionsDescEnabled.text()));
        substitutionsButton.setTooltipOff(List.of(ButtonToolTips.SubstitutionsOff.text(), ButtonToolTips.SubstitutionsDescDisabled.text()));
        widgets.add("substitutions", substitutionsButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        ArcaneInscriberMenu menu = getMenu();
        encodeButton.visible = !menu.alreadyStored;
        encodeButton.active = menu.coreInserted;
        eraseButton.visible = menu.alreadyStored;
        eraseButton.active = menu.coreInserted;
        substitutionsButton.setState(menu.substitute);
    }
}
