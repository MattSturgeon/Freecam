package net.xolt.freecam.gui.textures;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.texture.Scaling;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Reimplement a subset of vanilla Minecraft's atlas/sprite rendering so that we can draw
 * scaled textures without needing to use an atlas/spritesheet manager.
 */
public abstract class Texture {
    protected static final Logger LOGGER = LogUtils.getLogger();
    protected final Identifier identifier;
    protected final int textureWidth;
    protected final int textureHeight;

    protected Texture(Identifier identifier, int width, int height) {
        this.identifier = identifier;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public static Texture getTexture(Identifier identifier) {
        Scaling scaling = MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(identifier)
                .map(resource -> {
                    try {
                        return resource.getMetadata();
                    } catch (IOException e) {
                        LOGGER.error("Unable to parse metadata from {}", identifier, e);
                        return null;
                    }
                })
                .flatMap(metadata -> metadata.decode(GuiResourceMetadata.SERIALIZER))
                .orElse(GuiResourceMetadata.DEFAULT)
                .scaling();

        return switch (scaling.getType()) {
            case STRETCH -> new StretchTexture(identifier, (Scaling.Stretch) scaling);
            case TILE -> new TileTexture(identifier, (Scaling.Tile) scaling);
            case NINE_SLICE -> new NineSliceTexture(identifier, (Scaling.NineSlice) scaling);
        };
    }

    public abstract void draw(DrawContext context, int x, int y, int width, int height);

    protected void drawRegion(DrawContext context, int x, int y, int width, int height, int u, int v) {
        this.drawRegion(context, x, y, width, height, u, v, width, height);
    }

    protected void drawRegion(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        context.drawTexture(this.identifier, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    // Some textures need to be tiled instead of stretched
    // Based on net.minecraft.client.gui.DrawContext.drawSpriteTiled
    protected void drawRegionTiled(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
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
