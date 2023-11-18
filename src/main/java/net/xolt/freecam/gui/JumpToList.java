package net.xolt.freecam.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

public class JumpToList extends AlwaysSelectedEntryListWidget<JumpToListEntry> {
    private final JumpToScreen screen;

    public JumpToList(JumpToScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.screen = screen;
        this.setRenderBackground(false);
    }

    public void update() {
        JumpToListEntry selection = this.getSelectedOrNull();

        this.replaceEntries(client.world.getPlayers().stream()
                // TODO search filter
                // TODO sort
                .map(player -> new JumpToPlayerListEntry(this.client, this.screen, player))
                .map(JumpToListEntry.class::cast)
                .toList());

        this.setSelected(this.getFirst());
        if (selection != null) {
            this.children().stream()
                    .filter(selection::matches)
                    .findAny()
                    .ifPresent(this::setSelected);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        JumpToListEntry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
