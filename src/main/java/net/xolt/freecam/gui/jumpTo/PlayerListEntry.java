package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.xolt.freecam.util.FreecamPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerListEntry extends ListEntry {
    public static final int BLACK_COLOR = ColorHelper.Argb.getArgb(190, 0, 0, 0);
    public static final int GRAY_COLOR = ColorHelper.Argb.getArgb(255, 74, 74, 74);
    public static final int DARK_GRAY_COLOR = ColorHelper.Argb.getArgb(255, 48, 48, 48);
    public static final int WHITE_COLOR = ColorHelper.Argb.getArgb(255, 255, 255, 255);
    public static final int LIGHT_GRAY_COLOR = ColorHelper.Argb.getArgb(140, 255, 255, 255);

    private final @Nullable Supplier<SkinTextures> skinSupplier;
    private final Text name;
    private final UUID uuid;

    public PlayerListEntry(MinecraftClient client, JumpToScreen screen, PlayerEntity player) {
        super(client, screen, FreecamPosition.getSwimmingPosition(player));
        this.uuid = player.getUuid();

        MutableText name = Text.literal(player.getEntityName());
        if (Objects.equals(player, client.player)) {
            name.setStyle(Style.EMPTY.withItalic(false).withBold(true));
            name = Text.translatable("gui.freecam.jumpTo.entry.player.you", name).setStyle(Style.EMPTY.withItalic(true));
        }
        this.name = name;

        var networkPlayerEntry = this.client.player.networkHandler.getPlayerListEntry(this.uuid);
        this.skinSupplier = networkPlayerEntry == null ? null : networkPlayerEntry::getSkinTextures;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int fullEntryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        // We are passed a reduced entryHeight but the full entryWidth...
        int entryWidth = fullEntryWidth - 4;
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
        this.position = FreecamPosition.getSwimmingPosition(player);
    }

    @Override
    public String getName() {
        return this.name.getString();
    }

    @Override
    public boolean matches(String string) {
        return this.getName().toLowerCase(Locale.ROOT).contains(string);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public int compareTo(@NotNull ListEntry entry) {
        // Sort before non-player entries
        if (!(entry instanceof PlayerListEntry playerListEntry)) {
            return -1;
        }

        // Sort mc.player before other players
        if (Objects.equals(this.getUUID(), playerListEntry.getUUID())) {
            return 0;
        }
        if (Objects.equals(this.getUUID(), this.client.player.getUuid())) {
            return -1;
        }
        if (Objects.equals(playerListEntry.getUUID(), this.client.player.getUuid())) {
            return 1;
        }

        // Fallback to string compare
        return this.getName().compareTo(playerListEntry.getName());
    }
}
