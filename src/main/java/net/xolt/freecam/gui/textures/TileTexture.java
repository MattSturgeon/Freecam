package net.xolt.freecam.gui.textures;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Scaling;
import net.minecraft.util.Identifier;

public class TileTexture extends Texture {

    TileTexture(Identifier identifier, Scaling.Tile scaling) {
        super(identifier, scaling.width(), scaling.height());
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height) {
        if (width == textureWidth && height == textureHeight) {
            // Draw without scaling
            this.drawRegion(context, x, y, width, height, 0, 0, textureWidth, textureHeight);
            return;
        }

        this.drawRegionTiled(context, x, y, width, height, 0, 0,  textureWidth, textureHeight);
    }
}
