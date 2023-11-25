package net.xolt.freecam.gui.textures;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Scaling;
import net.minecraft.util.Identifier;

public class NineSliceTexture extends Texture {
    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    NineSliceTexture(Identifier identifier, Scaling.NineSlice scaling) {
        super(identifier, scaling.width(), scaling.height());
        this.left = scaling.border().left();
        this.right = scaling.border().right();
        this.top = scaling.border().top();
        this.bottom = scaling.border().bottom();
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height) {
        if (width == textureWidth && height == textureHeight) {
            // Draw in one
            this.drawRegion(context, x, y, width, height, 0, 0, textureWidth, textureHeight);
            return;
        }

        int left = Math.min(this.left, width / 2);
        int right = Math.min(this.right, width / 2);
        int top = Math.min(this.top, height / 2);
        int bottom = Math.min(this.bottom, height / 2);
        if (height == textureHeight) {
            // Left
            this.drawRegion(context, x, y, left, height, 0, 0, left, textureHeight);
            // Center
            this.drawRegionTiled(context, x + left, y, width - right - left, height, left, 0,  textureWidth - right - left, textureHeight);
            // Right
            this.drawRegion(context, x + width - right, y, right, height, textureWidth - right, 0, x + width - right, textureHeight);
            return;
        }
        if (width == textureWidth) {
            // Top
            this.drawRegion(context, x, y, width, top, 0, 0, textureWidth, top);
            // Center
            this.drawRegionTiled(context, x, y + top, width, height - bottom - top, 0, top, textureWidth, textureHeight - bottom - top);
            // Bottom
            this.drawRegion(context, x, y + height - bottom, width, bottom, 0, textureHeight - bottom, textureWidth, textureHeight - bottom);
            return;
        }
        // Top Left
        this.drawRegion(context, x, y, left, top, 0, 0);
        // Top Center
        this.drawRegionTiled(context, x + left, y, width - left - right, top, left, 0, textureWidth - left - right, top);
        // Top Right
        this.drawRegion(context, x + width - right, y, right, top, textureWidth - right, 0);
        // Bottom Left
        this.drawRegion(context, x, y + height - bottom, left, bottom, 0, textureHeight - bottom);
        // Bottom Center
        this.drawRegionTiled(context, x + left, y + height - bottom, width - left - right, bottom, left, textureHeight - bottom, textureWidth - left - right, bottom);
        // Bottom Right
        this.drawRegion(context, x + width - right, y + height - bottom, right, bottom, textureWidth - right, textureHeight - bottom);
        // Main Left
        this.drawRegionTiled(context, x, y + top, left, height - top - bottom, 0, top, left, textureHeight - top - bottom);
        // Main Center
        this.drawRegionTiled(context, x + left, y + top, width - left - right, height - top - bottom, left, top, textureWidth - left - right, textureHeight - top - bottom);
        // Main Right
        this.drawRegionTiled(context, x + width - right, y + top, right, height - top - bottom, textureWidth - right, top, right, textureHeight - top - bottom);
    }
}
