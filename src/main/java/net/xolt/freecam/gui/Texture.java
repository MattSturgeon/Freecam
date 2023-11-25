package net.xolt.freecam.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Reimplement a subset of vanilla Minecraft's {@link Sprite} rendering so that we can draw
 * scaled textures without needing to use {@link GuiAtlasManager}.
 */
public class Texture {
    private static final SpriteOpener SPRITE_OPENER = SpriteOpener.create(Set.of(AnimationResourceMetadata.READER, GuiResourceMetadata.SERIALIZER));

    private final Sprite sprite;
    private final Scaling scaling;

    public Texture(Identifier identifier) {
        SpriteContents contents = MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(identifier)
                .map(resource -> SPRITE_OPENER.loadSprite(identifier, resource))
                .orElseThrow(() -> new IllegalStateException("Unable to load sprite for %s".formatted(identifier)));

        this.sprite = new Sprite(identifier, contents, contents.getWidth(), contents.getHeight(), 0, 0);
        this.scaling = contents.getMetadata()
                .decode(GuiResourceMetadata.SERIALIZER)
                .orElse(GuiResourceMetadata.DEFAULT)
                .scaling();
    }

    public void draw(DrawContext context, int x, int y, int z, int width, int height) {
        // Based on net.minecraft.client.gui.DrawContext.drawGuiTexture(net.minecraft.util.Identifier, int, int, int, int, int)
        switch (scaling.getType()) {
            case STRETCH -> context.drawSprite(sprite, x, y, z, width, height);
            case NINE_SLICE -> context.drawSprite(sprite, (Scaling.NineSlice) scaling, x, y, z, width, height);
            case TILE -> {
                Scaling.Tile tile = (Scaling.Tile) scaling;
                context.drawSpriteTiled(sprite, x, y, z, width, height, 0, 0, tile.width(), tile.height(), tile.width(), tile.height());
            }
        }
    }
}
