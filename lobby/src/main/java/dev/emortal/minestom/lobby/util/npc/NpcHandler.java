package dev.emortal.minestom.lobby.util.npc;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import org.jetbrains.annotations.NotNull;

public interface NpcHandler {

    void handlePlayerInteract(@NotNull Player player, @NotNull ClickType clickType);
}
