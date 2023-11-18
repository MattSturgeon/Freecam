package net.xolt.freecam.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.xolt.freecam.Freecam;

import java.util.Optional;

public class JumpToScreen extends Screen {
    private static final int GUI_FRAME = 8;
    public static final int GUI_WIDTH = 236;
    private static final int GUI_TOP = 50;
    private static final int LIST_TOP = GUI_TOP + GUI_FRAME * 3;
    private static final int LIST_ITEM_HEIGHT = 36;

    private static final Identifier BACKGROUND = new Identifier(Freecam.ID, "textures/gui/jump_background.png");
    private static final int BG_TOP_H = GUI_FRAME;
    private static final int BG_CTR_H = 18;
    private static final int BG_BOTTOM_H = GUI_FRAME;
    private static final int BG_TOP = 0;
    private static final int BG_CTR = BG_TOP_H;
    private static final int BG_BOTTOM = BG_TOP_H + BG_CTR_H;
    private static final int BG_WIDTH = GUI_WIDTH;
    private static final int BG_HEIGHT = BG_TOP_H + BG_CTR_H + BG_BOTTOM_H;


    private JumpToList list;
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
            this.list = new JumpToList(this, this.client, this.width, this.height, LIST_TOP, this.getListBottom(), LIST_ITEM_HEIGHT);
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

        // Render top frame
        context.drawTexture(BACKGROUND, x, GUI_TOP, GUI_WIDTH, GUI_FRAME, 0, BG_TOP, BG_WIDTH, BG_TOP_H, BG_WIDTH, BG_HEIGHT);
        // Render GUI center
        context.drawTexture(BACKGROUND, x, GUI_TOP + GUI_FRAME, GUI_WIDTH, ctrHeight, 0, BG_CTR, BG_WIDTH, BG_CTR_H, BG_WIDTH, BG_HEIGHT);
        // Render bottom frame
        context.drawTexture(BACKGROUND, x, GUI_TOP + GUI_FRAME + ctrHeight, GUI_WIDTH, GUI_FRAME, 0, BG_BOTTOM, BG_WIDTH, BG_BOTTOM_H, BG_WIDTH, BG_HEIGHT);
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
        return Math.max(52, this.height - (GUI_TOP *2) - (GUI_FRAME *2));
    }

    private int getListBottom() {
        return GUI_TOP + GUI_FRAME + this.getScreenHeight();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.initialized) {
            this.list.update();
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

    public void select(JumpToListEntry entry) {
        this.list.setSelected(entry);
        this.updateButtonState();
    }

    private void updateButtonState() {
        // TODO enable/disable buttons based on selected entry
    }

    public void jump() {
        // TODO jump to selected
        Optional.ofNullable(this.list.getSelectedOrNull())
                .ifPresent(JumpToListEntry::jump);
    }
}
