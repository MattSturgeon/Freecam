package net.xolt.freecam.mixins.accessors;

import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(BooleanListEntry.class)
public interface BooleanListEntryAccessor {

    @Final
    @Accessor(value = "bool", remap = false)
    AtomicBoolean getAtomicBoolean();

    @Final
    @Accessor(value = "buttonWidget", remap = false)
    ButtonWidget getButtonWidget();

    @Final
    @Mutable
    @Accessor(value = "buttonWidget", remap = false)
    void setButtonWidget(ButtonWidget widget);

    @Final
    @Accessor(value = "resetButton", remap = false)
    ButtonWidget getResetButton();

    @Final
    @Mutable
    @Accessor(value = "resetButton", remap = false)
    void setResetButton(ButtonWidget widget);

    @Final
    @Accessor(value = "widgets", remap = false)
    List<ClickableWidget> getChildWidgets();

    @Final
    @Mutable
    @Accessor(value = "widgets", remap = false)
    void setChildWidgets(List<ClickableWidget> widgets);


}
