package net.xolt.freecam.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.xolt.freecam.util.FreecamPosition;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.GRAY_COLOR;
import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public class JumpToPlayerListEntry extends JumpToListEntry {

    private final @Nullable Supplier<SkinTextures> skinSupplier;
    private final String name;
    private final UUID uuid;

    public JumpToPlayerListEntry(MinecraftClient client, JumpToScreen screen, PlayerEntity player) {
        super(client, screen, FreecamPosition.getSwimmingPosition(player));
        this.uuid = player.getUuid();
        this.name = player.getEntityName();
        PlayerListEntry playerListEntry = this.client.player.networkHandler.getPlayerListEntry(this.uuid);
        this.skinSupplier = playerListEntry == null ? null : playerListEntry::getSkinTextures;
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

    @Override
    public boolean matches(JumpToListEntry entry) {
        if (entry instanceof JumpToPlayerListEntry pEntry) {
            return pEntry.uuid.equals(this.uuid);
        }
        return false;
    }
}
