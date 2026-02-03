package dev.emortal.minestom.lobby.features;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface LobbyFeature {
    void register(@NotNull Instance instance);
}
