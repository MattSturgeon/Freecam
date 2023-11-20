package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class JumpToScreen extends Screen {
    private static final int GUI_BOTTOM_FRAME = 8;
    private static final int GUI_WIDTH = 236;
    private static final int GUI_TOP = 50;
    private static final int LIST_TOP = GUI_TOP + 24;
    private static final int LIST_ITEM_HEIGHT = 36;
    private static final int GUI_BUTTON_ROW = 24;

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
        int listBottom = GUI_TOP + this.getGuiHeight() - GUI_BOTTOM_FRAME - GUI_BUTTON_ROW;

        if (this.initialized) {
            this.list.updateSize(this.width, this.height, LIST_TOP, listBottom);
        } else {
            this.playerEntryCache = new PlayerEntryCache(this.client, this);
            this.list = new ListWidget(this, this.client, this.width, this.height, LIST_TOP, listBottom, LIST_ITEM_HEIGHT);
        }

        int innerWidth = this.list.getRowWidth();
        int innerX = (this.width - innerWidth) / 2;

        this.addDrawableChild(this.list);
        this.setInitialFocus(this.list);

        SimplePositioningWidget positioner = new SimplePositioningWidget(innerX, listBottom, innerWidth, 0);
        positioner.getMainPositioner()
                .alignBottom()
                .alignRight()
                .margin(0);

        DirectionalLayoutWidget layout = positioner.add(DirectionalLayoutWidget.horizontal());
        layout.getMainPositioner()
                .alignBottom()
                .marginX(2)
                .marginY(0);

        layout.add(ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.back"), button -> this.client.setScreen(null)).width(48).build());
        this.buttonJump = layout.add(ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.jump"), button -> this.jump()).width(48).build());

        positioner.refreshPositions();
        positioner.forEachChild(this::addDrawableChild);

        this.initialized = true;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int left = (this.width - GUI_WIDTH) / 2;
        BackgroundTexture.render(context, left, GUI_TOP, GUI_WIDTH, this.getGuiHeight());
    }

    // GUI height
    private int getGuiHeight() {
        return Math.max(52, this.height - (GUI_TOP *2));
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

    public void updateButtonState() {
        ListEntry selected = this.list.getSelectedOrNull();
        this.buttonJump.active = selected != null;
    }

    public void jump() {
        Optional.ofNullable(this.list.getSelectedOrNull())
                .ifPresent(ListEntry::jump);
    }
}
