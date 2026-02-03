package dev.emortal.minestom.lobby.util;

import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;

public final class ProtoConverter {

    private ProtoConverter() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static @NotNull PlayerSkin convert(@NotNull dev.emortal.api.model.common.PlayerSkin skin) {
        return new PlayerSkin(skin.getTexture(), skin.getSignature());
    }

}
