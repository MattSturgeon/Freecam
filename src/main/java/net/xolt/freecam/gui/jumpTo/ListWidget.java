package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

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

        this.setSelected(this.getFirst());
        if (selection != null) {
            this.children().stream()
                    .filter(selection::equals)
                    .findAny()
                    .ifPresent(this::setSelected);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ListEntry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
