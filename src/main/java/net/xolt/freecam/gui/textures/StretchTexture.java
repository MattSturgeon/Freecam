package net.xolt.freecam.gui.textures;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Scaling;
import net.minecraft.util.Identifier;

public class StretchTexture extends Texture {

    StretchTexture(Identifier identifier, Scaling.Stretch scaling) {
        super(identifier, 0, 0);
        throw new IllegalStateException("StretchTexture is not implemented!");
        // FIXME Stretch scaling does not define texture width/height.
        //       Vanilla Sprites get this when reading the NativeImage.
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height) {
        this.drawRegion(context, x, y, width, height, 0, 0,  textureWidth, textureHeight);
    }
}
