package dev.emortal.minestom.lobby.game;

import dev.emortal.api.liveconfigparser.configs.common.ConfigItem;
import dev.emortal.api.liveconfigparser.configs.common.ConfigMap;
import dev.emortal.api.liveconfigparser.configs.gamemode.GameModeConfig;
import dev.emortal.api.service.matchmaker.ChangeMapVoteResult;
import dev.emortal.api.service.matchmaker.MatchmakerService;
import dev.emortal.api.service.matchmaker.QueueOptions;
import dev.emortal.api.service.matchmaker.QueuePlayerResult;
import dev.emortal.minestom.core.module.matchmaker.CommonMatchmakerError;
import io.grpc.StatusRuntimeException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// TODO re-render or close on mode config update
public final class MapSelectorInventory extends Inventory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapSelectorInventory.class);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // TODO: Switch messages to new system used in Velocity core
    private static final String TITLE_FORMAT = "<dark_green><mode> Map Selection</dark_green>";
    private static final String ERR_MAP_NOT_FOUND = "<red>Map <map> not found for <mode></red>";
    private static final Component ERR_UNKNOWN = Component.text("An unknown error occurred", NamedTextColor.RED);

    private final GameModeConfig mode;
    private final MatchmakerService matchmaker;
    private final boolean isQueued;

    private final Map<Integer, ConfigMap> slotMaps = new HashMap<>();

    /**
     * @param mode       the mode to select a map for
     * @param matchmaker instance of the matchmaker
     * @param isQueued   if queued, instead of queueing for a map it will modify the current queue's selection.
     */
    public MapSelectorInventory(@NotNull GameModeConfig mode, @NotNull MatchmakerService matchmaker, boolean isQueued) {
        super(InventoryType.CHEST_3_ROW, MINI_MESSAGE.deserialize(TITLE_FORMAT, Placeholder.unparsed("mode", mode.friendlyName())));

        this.isQueued = isQueued;
        this.matchmaker = matchmaker;
        this.mode = mode;

        this.loadMaps(mode);
        super.eventNode().addListener(InventoryPreClickEvent.class, this::handleClick);
    }

    private void loadMaps(@NotNull GameModeConfig config) {
        Map<String, ConfigMap> maps = config.maps();
        if (maps == null) return;

        for (ConfigMap map : maps.values()) {
            ConfigItem item = map.displayItem();
            this.slotMaps.put(item.slot(), map);

            ItemStack stack = ConfigItemConverter.convert(item, ConfigItemConverter.convertLore(item));
            if (stack == null) continue;

            super.setItemStack(item.slot(), stack);
        }
    }

    private void handleClick(@NotNull InventoryPreClickEvent event) {
        Player player = event.getPlayer();
        int slot = event.getSlot();

        event.setCancelled(true);

        ConfigMap map = this.slotMaps.get(slot);
        if (map == null) return; // nothing in slot

        if (this.isQueued) {
            Thread.startVirtualThread(() -> this.changeQueuedMap(player, map));
        } else {
            Thread.startVirtualThread(() -> this.queueForMap(player, map));
        }
        player.closeInventory();
    }

    @Blocking
    private void changeQueuedMap(@NotNull Player player, @NotNull ConfigMap map) {
        if (this.matchmaker == null) {
            LOGGER.warn("Failed to change map vote for '{}' on game '{}' to '{}' because matchmaker is unavailable",
                    player.getUsername(), this.mode.friendlyName(), map.friendlyName());
            return;
        }

        ChangeMapVoteResult result;
        try {
            result = this.matchmaker.changeMapVote(player.getUuid(), map.id());
        } catch (StatusRuntimeException exception) {
            LOGGER.error("Failed to change queued map for '{}' to '{}'", player.getUsername(), map.id(), exception);
            player.sendMessage(ERR_UNKNOWN);
            return;
        }

        switch (result) {
            case SUCCESS -> player.sendMessage(Component.text("You have changed your queued map to " + map.friendlyName(), NamedTextColor.GREEN));
            case INVALID_MAP -> {
                LOGGER.error("Player '{}' tried to change map to invalid map '{}'", player.getUuid(), map.id());

                var mapName = Placeholder.unparsed("map", map.friendlyName());
                var modeName = Placeholder.unparsed("mode", this.mode.friendlyName());
                player.sendMessage(MINI_MESSAGE.deserialize(ERR_MAP_NOT_FOUND, mapName, modeName));
            }
            case NOT_IN_QUEUE -> {
                LOGGER.error("Player '{}' is not in queue but tried to change map", player.getUuid());
                player.sendMessage(ERR_UNKNOWN);
            }
        }
    }

    @Blocking
    private void queueForMap(@NotNull Player player, @NotNull ConfigMap map) {
        if (this.matchmaker == null) {
            LOGGER.warn("Failed to queue '{}' for '{}' on map '{}' because matchmaker is unavailable",
                    player.getUsername(), this.mode.friendlyName(), map.friendlyName());
            return;
        }

        QueuePlayerResult result;
        try {
            result = this.matchmaker.queuePlayer(this.mode.id(), player.getUuid(), QueueOptions.builder().mapId(map.id()).build());
        } catch (StatusRuntimeException exception) {
            LOGGER.error("Failed to queue '{}' for map '{}' on '{}'", player.getUsername(), map.friendlyName(), this.mode.friendlyName(), exception);
            player.sendMessage(ERR_UNKNOWN);
            return;
        }

        switch (result) {
            case SUCCESS -> player.sendMessage(Component.text("You have been queued for " + this.mode.friendlyName(), NamedTextColor.GREEN));
            case ALREADY_IN_QUEUE -> player.sendMessage(CommonMatchmakerError.QUEUE_ERR_ALREADY_IN_QUEUE);
            case PARTY_TOO_LARGE -> {
                var modeName = Placeholder.unparsed("mode", this.mode.friendlyName());
                var maxSize = Placeholder.unparsed("max", String.valueOf(this.mode.partyRestrictions().maxSize()));
                player.sendMessage(MINI_MESSAGE.deserialize(CommonMatchmakerError.QUEUE_ERR_PARTY_TOO_LARGE, modeName, maxSize));
            }
            case GAME_MODE_DISABLED, INVALID_GAME_MODE -> {
                var modeName = Placeholder.unparsed("mode", this.mode.friendlyName());
                player.sendMessage(MINI_MESSAGE.deserialize(CommonMatchmakerError.QUEUE_ERR_UNKNOWN, modeName));
            }
            case INVALID_MAP -> {
                var mapName = Placeholder.unparsed("map", map.friendlyName());
                var modeName = Placeholder.unparsed("mode", this.mode.friendlyName());
                player.sendMessage(MINI_MESSAGE.deserialize(ERR_MAP_NOT_FOUND, mapName, modeName));
            }
            case NO_PERMISSION -> player.sendMessage(Component.text("You do not have permission to queue for " + this.mode.friendlyName(), NamedTextColor.RED));
        }
    }
}
