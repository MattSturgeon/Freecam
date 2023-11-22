package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.xolt.freecam.Freecam;

public class ListBackgroundTexture {
    private static final Identifier TEXTURE = new Identifier(Freecam.ID, "textures/gui/jump_list_background.png");
    private static final int WIDTH = 3;
    private static final int HEIGHT = 3;
    private static final Segment TOP_LEFT = new Segment(0, 0, 1, 1);
    private static final Segment TOP = new Segment(1, 0, 1, 1);
    private static final Segment TOP_RIGHT = new Segment(2, 0, 1, 1);
    private static final Segment LEFT = new Segment(0, 1, 1, 1);
    private static final Segment CENTER = new Segment(1, 1, 1, 1);
    private static final Segment RIGHT = new Segment(2, 1, 1, 1);
    private static final Segment BOTTOM_LEFT = new Segment(0, 2, 1, 1);
    private static final Segment BOTTOM = new Segment(1, 2, 1, 1);
    private static final Segment BOTTOM_RIGHT = new Segment(2, 2, 1, 1);

    private ListBackgroundTexture() {}

    public static void render(DrawContext context, int x, int y, int width, int height) {
        int leftWidth = LEFT.width();
        int rightWidth = LEFT.width();
        int centerWidth = width - leftWidth - rightWidth;

        int topHeight = TOP.height();
        int bottomHeight = BOTTOM.height();
        int centerHeight = height - topHeight - bottomHeight;

        int leftX = x;
        int centerX = leftX + leftWidth;
        int rightX = centerX + centerWidth;

        int topY = y;
        int centerY = topY + topHeight;
        int bottomY = centerY + centerHeight;

        TOP_LEFT.render(context, leftX, topY, leftWidth, topHeight);
        TOP.render(context, centerX, topY, centerWidth, topHeight);
        TOP_RIGHT.render(context, rightX, topY, rightWidth, topHeight);

        LEFT.render(context, leftX, centerY, leftWidth, centerHeight);
        CENTER.render(context, centerX, centerY, centerWidth, centerHeight);
        RIGHT.render(context, rightX, centerY, rightWidth, centerHeight);

        BOTTOM_LEFT.render(context, leftX, bottomY, leftWidth, bottomHeight);
        BOTTOM.render(context, centerX, bottomY, centerWidth, bottomHeight);
        BOTTOM_RIGHT.render(context, rightX, bottomY, rightWidth, bottomHeight);
    }

    private record Segment(float u, float v, int width, int height) {
        public void render(DrawContext context, int x, int y, int width, int height) {
            context.drawTexture(
                    TEXTURE,
                    x,
                    y,
                    width,
                    height,
                    this.u(),
                    this.v(),
                    this.width(),
                    this.height(),
                    WIDTH,
                    HEIGHT
                );
        }
    }
}
