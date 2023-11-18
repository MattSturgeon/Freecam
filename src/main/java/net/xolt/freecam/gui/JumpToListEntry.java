package net.xolt.freecam.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.xolt.freecam.util.FreecamPosition;

public abstract class JumpToListEntry extends AlwaysSelectedEntryListWidget.Entry<JumpToListEntry> {
    protected final MinecraftClient client;
    protected final JumpToScreen screen;
    protected final FreecamPosition position;
    private long time;

    protected JumpToListEntry(MinecraftClient client, JumpToScreen screen, FreecamPosition position) {
        this.client = client;
        this.screen = screen;
        this.position = position;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.screen.select(this);
        if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.jump();
        }
        this.time = Util.getMeasuringTimeMs();
        return false;
    }

    @Override
    public Text getNarration() {
        // FIXME
        return null;
    }

    public void jump() {
        // TODO jump to the target
//        this.position;
        System.out.println("jumping");
    }

    // Fuzzy match; does this entry refer to the same thing, even if the exact location has changed
    public abstract boolean matches(JumpToListEntry entry);
}
