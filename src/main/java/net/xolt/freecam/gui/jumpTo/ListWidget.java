package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListWidget extends AlwaysSelectedEntryListWidget<ListEntry> {
    private final JumpToScreen screen;

    public ListWidget(JumpToScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.setRenderBackground(false);
    }

    public void updateEntries(List<ListEntry> newEntries) {
        ListEntry selection = this.getSelectedOrNull();

        this.replaceEntries(newEntries);

        // We only want to set the selection if the old selection is missing
        if (selection == null || !this.children().contains(selection)) {
            this.setSelected(this.getFirst());
        }
    }

    @Override
    public @Nullable ListEntry getFirst() {
        // Prevent IndexOutOfBoundsException
        return this.children().isEmpty() ? null : super.getFirst();
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
}
