package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.xolt.freecam.util.FreecamPosition;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.GRAY_COLOR;
import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public class PlayerListEntry extends ListEntry {

    private final @Nullable Supplier<SkinTextures> skinSupplier;
    private final String name;
    private final UUID uuid;

    public PlayerListEntry(MinecraftClient client, JumpToScreen screen, PlayerEntity player) {
        super(client, screen, FreecamPosition.getSwimmingPosition(player));
        this.uuid = player.getUuid();
        this.name = player.getEntityName();
        var networkPlayerEntry = this.client.player.networkHandler.getPlayerListEntry(this.uuid);
        this.skinSupplier = networkPlayerEntry == null ? null : networkPlayerEntry::getSkinTextures;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int padding = 4;
        int skinSize = 24;
        boolean hasSkin = this.skinSupplier != null;

        context.fill(x, y, x + entryWidth, y + entryHeight, GRAY_COLOR);

        if (hasSkin) {
            int skinX = x + padding;
            int skinY = y + (entryHeight - skinSize) / 2;
            PlayerSkinDrawer.draw(context, this.skinSupplier.get(), skinX, skinY, skinSize);
        }

        int textX = x + padding + (hasSkin ? skinSize + padding : 0);
        int textY = y + (entryHeight - this.client.textRenderer.fontHeight) / 2;
        context.drawText(this.client.textRenderer, this.name, textX, textY, WHITE_COLOR, false);
    }

    public void update(PlayerEntity player) {
        // TODO update location of player
    }
}
