package net.xolt.freecam.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Scaling;
import net.minecraft.util.Identifier;

/**
 * Reimplement a subset of vanilla Minecraft's atlas/sprite rendering so that we can draw
 * actual Nine Slice textures without needing to use an atlas/spritesheet manager.
 */
public class NineSliceTexture {
    private final Identifier identifier;
    private final int textureWidth;
    private final int textureHeight;
    private final Scaling.NineSlice.Border border;

    public NineSliceTexture(Identifier identifier, int width, int height, int border) {
        this(identifier, new Scaling.NineSlice(width, height, new Scaling.NineSlice.Border(border, border, border, border)));
    }

    public NineSliceTexture(Identifier identifier, Scaling.NineSlice scaling) {
        this.identifier = identifier;
        this.textureWidth = scaling.width();
        this.textureHeight = scaling.height();
        this.border = scaling.border();
    }

    public final void draw(DrawContext context, int x, int y, int width, int height) {
        int left = Math.min(border.left(), width / 2);
        int right = Math.min(border.right(), width / 2);
        int top = Math.min(border.top(), height / 2);
        int bottom = Math.min(border.bottom(), height / 2);

        if (width == textureWidth && height == textureHeight) {
            // Draw in one
            context.drawTexture(identifier, x, y, width, height, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            return;
        }
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

    public void drawRegion(DrawContext context, int x, int y, int width, int height, int u, int v) {
        this.drawRegion(context, x, y, width, height, u, v, width, height);
    }

    public void drawRegion(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        context.drawTexture(this.identifier, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    // Some textures need to be tiled instead of stretched
    // Based on net.minecraft.client.gui.DrawContext.drawSpriteTiled
    private void drawRegionTiled(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (regionWidth <= 0 || regionHeight <= 0) {
            throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + regionWidth + "x" + regionHeight);
        }

        // xyProgress track how many pixels have been rendered on each axis.
        // xyNext is the number of pixels to be drawn during the current iteration,
        // clamped using Math.min to avoid overflowing past width/height
        for (int xProgress = 0; xProgress < width; xProgress += regionWidth) {
            int xNext = Math.min(regionWidth, width - xProgress);
            for (int yProgress = 0; yProgress < height; yProgress += regionHeight) {
                int yNext = Math.min(regionHeight, height - yProgress);
                this.drawRegion(context, x + xProgress, y + yProgress, xNext, yNext, u, v, regionWidth, regionHeight);
            }
        }
    }
}
