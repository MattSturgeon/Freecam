package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class JumpToScreen extends Screen {
    private static final int GUI_FRAME = 8;
    private static final int GUI_WIDTH = 236;
    private static final int GUI_TOP = 50;
    private static final int LIST_TOP = GUI_TOP + GUI_FRAME * 3;
    private static final int LIST_ITEM_HEIGHT = 36;


    private PlayerEntryCache playerEntryCache;
    private ListWidget list;
    private boolean initialized;
    private ButtonWidget buttonJump;

    public JumpToScreen() {
        super(Text.translatable("gui.freecam.jumpTo.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (this.initialized) {
            this.list.updateSize(this.width, this.height, LIST_TOP, this.getListBottom());
        } else {
            this.playerEntryCache = new PlayerEntryCache(this.client, this);
            this.list = new ListWidget(this, this.client, this.width, this.height, LIST_TOP, this.getListBottom(), LIST_ITEM_HEIGHT);
        }
        this.addSelectableChild(this.list);
        this.buttonJump = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.jump"), button -> this.jump()).width(48).build());
        DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
        AxisGridWidget grid = layout.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        grid.add(this.buttonJump);
        SimplePositioningWidget.setPos(layout, 0, this.height - this.getListBottom(), this.width, this.getListBottom() - 20);
        this.initialized = true;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int x = (this.width - GUI_WIDTH + 4) / 2;
        int ctrHeight = this.getScreenHeight();

        BackgroundTexture.render(context, x, GUI_TOP, GUI_WIDTH, ctrHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (!this.list.children().isEmpty()) {
            this.list.render(context, mouseX, mouseY, delta);
        }
    }

    // GUI height excluding frame
    private int getScreenHeight() {
        return Math.max(52, this.height - (GUI_TOP *2));
    }

    private int getListBottom() {
        return GUI_TOP  + this.getScreenHeight() - GUI_FRAME;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.initialized) {
            this.updateEntries();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.list.getSelectedOrNull() != null) {
            if (KeyCodes.isToggle(keyCode)) {
                this.jump();
                return true;
            }
            return this.list.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void updateEntries() {
        List<ListEntry> playerEntries = client.world.getPlayers()
                .stream()
                // TODO search filter
                // TODO sort
                .map(this.playerEntryCache::createOrUpdate)
                .map(ListEntry.class::cast)
                .toList();

        this.list.updateEntries(playerEntries);
    }

    public void select(ListEntry entry) {
        this.list.setSelected(entry);
        this.updateButtonState();
    }

    private void updateButtonState() {
        // TODO enable/disable buttons based on selected entry
    }

    public void jump() {
        // TODO jump to selected
        Optional.ofNullable(this.list.getSelectedOrNull())
                .ifPresent(ListEntry::jump);
    }
}
