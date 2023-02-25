package net.xolt.freecam.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiTransformer;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.xolt.freecam.mixins.accessors.BooleanListEntryAccessor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Retention(RetentionPolicy.RUNTIME)
@interface ListenableConfigEntry {
    GuiTransformer transformer = (list, translationKey, field, o, o1, guiRegistryAccess) -> {
        System.err.println("Found match (transformer): " + field.getName());
        if (list.size() != 1) throw new AssertionError("Expected ConfigListEntry list to contain exactly 1 entry.");

        BooleanListEntry entry = list.stream()
                .filter(BooleanListEntry.class::isInstance)
                .map(BooleanListEntry.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ListenableConfigEntry currently only supports Boolean types."));

        // Get needed data from the BooleanListEntry
        AtomicBoolean bool = ((BooleanListEntryAccessor) entry).getAtomicBoolean();
        List<ClickableWidget> widgets = ((BooleanListEntryAccessor) entry).getChildWidgets();

        // Ensure nothing weird is going on
        if (widgets.size() != 2) throw new AssertionError("Expected BooleanListEntry to have exactly 2 children.");

        // Create replacement buttons that will trigger events
        ButtonWidget buttonWidget = ButtonWidget.builder(widgets.get(0).getMessage(), (widget) -> {
                    boolean value = !bool.get();
                    System.err.println("Setting value of " + field.getName() + " to " + value);
                    bool.set(value);
                })
                .dimensions(widgets.get(0).getX(), widgets.get(0).getY(), widgets.get(0).getWidth(), widgets.get(0).getHeight())
                .build();

        ButtonWidget resetButton = ButtonWidget.builder(widgets.get(1).getMessage(), (widget) -> entry.getDefaultValue().ifPresent(value -> {
                    System.err.println("Resetting " + field.getName() + " to " + value);
                    bool.set(value);
                }))
                .dimensions(widgets.get(1).getX(), widgets.get(1).getY(), widgets.get(1).getWidth(), widgets.get(1).getHeight())
                .build();

        // Mutate the BooleanListEntry, replacing its buttons
        ((BooleanListEntryAccessor) entry).setChildWidgets(List.of(buttonWidget, resetButton));
        ((BooleanListEntryAccessor) entry).setButtonWidget(buttonWidget);
        ((BooleanListEntryAccessor) entry).setResetButton(resetButton);

        return list;
    };
}
