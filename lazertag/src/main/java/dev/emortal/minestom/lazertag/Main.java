package dev.emortal.minestom.lazertag;

import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.map.MapManager;
import dev.emortal.minestom.lazertag.raycast.RaycastUtil;

public final class Main {
    private static final int MIN_PLAYERS = 2;

    void main() {
        RaycastUtil.init();

        MinestomGameServer.create((a) -> {
            MapManager mapManager = new MapManager();

            return GameSdkConfig.builder()
                    .minPlayers(MIN_PLAYERS)
                    .gameCreator(info -> new LazerTagGame(info, mapManager.getMap(info.mapId())))
                    .build();
        });
    }
}
