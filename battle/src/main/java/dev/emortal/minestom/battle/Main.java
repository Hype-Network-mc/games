package dev.emortal.minestom.battle;

import dev.emortal.minestom.battle.game.BattleGame;
import dev.emortal.minestom.battle.map.MapManager;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import io.github.togar2.pvp.MinestomPvP;
import net.minestom.server.MinecraftServer;

public final class Main {
    private static final int MIN_PLAYERS = 2;

    static void main() {
        MinestomGameServer.create((a) -> {
            MinestomPvP.init();
            MinecraftServer.getConnectionManager().setPlayerProvider(PermissionCustomPlayer::new);

            MapManager mapManager = new MapManager();

            return GameSdkConfig.builder()
                    .minPlayers(MIN_PLAYERS)
                    .gameCreator(info -> new BattleGame(info, mapManager.getMap(info.mapId())))
                    .build();
        });
    }
}