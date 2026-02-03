package dev.emortal.minestom.lobby.game;

import dev.emortal.api.liveconfigparser.configs.gamemode.GameModeConfig;
import dev.emortal.api.service.matchmaker.MatchmakerService;
import dev.emortal.api.service.matchmaker.QueuePlayerResult;
import dev.emortal.minestom.core.module.matchmaker.CommonMatchmakerError;
import io.grpc.StatusRuntimeException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class QueueGameClickHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueGameClickHandler.class);

    static void leftClick(@NotNull Player player, @NotNull GameModeConfig config, @Nullable MatchmakerService matchmaker) {
        if (matchmaker == null) {
            LOGGER.warn("Player {} queued for {}, but matchmaker is unavailable.", player.getUsername(), config.friendlyName());
            return;
        }

        QueuePlayerResult result;
        try {
            result = matchmaker.queuePlayer(config.id(), player.getUuid());
        } catch (StatusRuntimeException exception) {
            LOGGER.error("Failed to queue '{}' for '{}'", player.getUsername(), config.friendlyName(), exception);
            return;
        }

        switch (result) {
            case SUCCESS -> player.sendMessage(Component.text()
                    .append(Component.text("You have been queued for ", NamedTextColor.GREEN))
                    .append(MiniMessage.miniMessage().deserialize(config.displayItem().name()))
                    .build());
            case ALREADY_IN_QUEUE -> player.sendMessage(CommonMatchmakerError.QUEUE_ERR_ALREADY_IN_QUEUE);
            case PARTY_TOO_LARGE -> {
                var modeName = Placeholder.unparsed("mode", config.friendlyName());
                var maxSize = Placeholder.unparsed("max", String.valueOf(config.partyRestrictions().maxSize()));
                player.sendMessage(MiniMessage.miniMessage().deserialize(CommonMatchmakerError.QUEUE_ERR_PARTY_TOO_LARGE, modeName, maxSize));
            }
            case GAME_MODE_DISABLED, INVALID_GAME_MODE, INVALID_MAP -> {
                var modeName = Placeholder.parsed("mode", config.displayItem().name());
                player.sendMessage(MiniMessage.miniMessage().deserialize(CommonMatchmakerError.QUEUE_ERR_UNKNOWN, modeName));
            }
            case NO_PERMISSION -> player.sendMessage(Component.text("You must be the party leader to queue for games", NamedTextColor.RED));
        }
    }

    static void rightClick(@NotNull Player player, @NotNull GameModeConfig config, @Nullable MatchmakerService matchmaker) {
        if (matchmaker == null) {
            LOGGER.warn("Player '{}' tried to select map for game mode '{}', but matchmaker is unavailable.",
                    player.getUsername(), config.friendlyName());
            return;
        }

        if (config.maps() == null || config.maps().isEmpty()) {
            leftClick(player, config, matchmaker);
            return;
        }

        // actual right click logic
        MapSelectorInventory mapInventory = new MapSelectorInventory(config, matchmaker, false);
        player.openInventory(mapInventory);
    }

    private QueueGameClickHandler() {
    }
}
