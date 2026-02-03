package dev.emortal.minestom.lobby;

import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public final class LobbyTags {
    public static final @NotNull Tag<Boolean> LOBBABLE = Tag.Boolean("lobbable");

    private LobbyTags() {
    }
}
