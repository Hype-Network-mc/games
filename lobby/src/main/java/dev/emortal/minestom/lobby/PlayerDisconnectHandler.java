package dev.emortal.minestom.lobby;

import dev.agones.sdk.AgonesSDKProto;
import dev.agones.sdk.SDKGrpc;
import dev.emortal.api.agonessdk.IgnoredStreamObserver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.jetbrains.annotations.NotNull;

final class PlayerDisconnectHandler {

    private final @NotNull SDKGrpc.SDKStub sdk;

    PlayerDisconnectHandler(@NotNull SDKGrpc.SDKStub sdk) {
        this.sdk = sdk;

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> this.onDisconnect(event.getPlayer()));
    }

    void onDisconnect(@NotNull Player player) {
        int newPlayerCount = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        if (newPlayerCount > 0) return;

        this.sdk.ready(AgonesSDKProto.Empty.getDefaultInstance(), new IgnoredStreamObserver<>());
    }
}
