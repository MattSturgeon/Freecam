package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ListWidget extends AlwaysSelectedEntryListWidget<ListEntry> {
    private final JumpToScreen screen;

    public ListWidget(JumpToScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.setRenderBackground(false);
    }

    public void updateEntries(List<ListEntry> newEntries) {
        ListEntry selection = migrateSelection(this.getSelectedOrNull(), newEntries, this.children());
        this.replaceEntries(newEntries);
        this.setSelected(selection);
    }

    @Override
    public void setSelected(@Nullable ListEntry entry) {
        super.setSelected(entry);
        this.screen.updateButtonState();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ListEntry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static @Nullable ListEntry migrateSelection(ListEntry selection, List<ListEntry> newEntries, List<ListEntry> oldEntries) {
        // New list is empty, can't select anything
        if (newEntries.isEmpty()) {
            return null;
        }

        // No previous selection existed, nothing to check
        if (selection == null) {
            return newEntries.get(0);
        }

        int len = oldEntries.size();
        int index = oldEntries.indexOf(selection);

        // Helper function to check candidates
        Predicate<Integer> check = i -> {
            ListEntry entry = oldEntries.get(i);
            return newEntries.contains(entry);
        };

        // Check if the previous selection is still present
        if (index >= 0 && index < len && check.test(index)) {
            return selection;
        }

        // Use a "nearest neighbor" style search when the selection is lost,
        // to minimise GUI focus jumps and improve UX.
        int dec = index;
        int inc = index;
        while (true) {
            dec--;
            inc++;

            // Failure: out-of-bounds in both directions
            // terminate the loop
            if (dec < 0 && inc >= len) {
                return newEntries.get(0);
            }

            // Check for a lower neighbor
            if (dec >= 0 && check.test(dec)) {
                return oldEntries.get(dec);
            }

            // Check for a higher neighbor
            if (inc < len && check.test(inc)) {
                return oldEntries.get(inc);
            }

        }
    }
}
