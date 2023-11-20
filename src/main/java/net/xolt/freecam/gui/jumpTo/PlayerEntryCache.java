package net.xolt.freecam.gui.jumpTo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEntryCache {
    private final MinecraftClient client;
    private final JumpToScreen screen;
    private final Map<UUID, PlayerListEntry> map;

    public PlayerEntryCache(MinecraftClient client, JumpToScreen screen) {
        this.client = client;
        this.screen = screen;
        map = new HashMap<>();
    }

    public PlayerListEntry createOrUpdate(PlayerEntity player) {
        return map.compute(player.getUuid(), (uuid, entry) -> {
            if (entry == null) {
                return new PlayerListEntry(this.client, this.screen, player);
            }
            entry.update(player);
            return entry;
        });
    }
}
