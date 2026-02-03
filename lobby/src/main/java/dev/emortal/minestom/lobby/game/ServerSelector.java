package dev.emortal.minestom.lobby.game;

import dev.emortal.api.liveconfigparser.configs.ConfigProvider;
import dev.emortal.api.liveconfigparser.configs.ConfigUpdate;
import dev.emortal.api.liveconfigparser.configs.common.ConfigItem;
import dev.emortal.api.liveconfigparser.configs.gamemode.GameModeConfig;
import dev.emortal.api.service.matchmaker.MatchmakerService;
import dev.emortal.api.service.playertracker.PlayerTrackerService;
import dev.emortal.minestom.lobby.LobbyEvents;
import io.grpc.StatusRuntimeException;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.*;

public final class ServerSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSelector.class);

    private final @Nullable MatchmakerService matchmaker;
    private final @Nullable PlayerTrackerService playerTracker;
    private final @NotNull ConfigProvider<GameModeConfig> configProvider;
    private final @NotNull GameNpcHandler npcHandler;

    private final Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, "Pick a game, any game!");
    private final Map<Integer, GameModeConfig> slotToGameMode = new HashMap<>();

    public ServerSelector(@NotNull Instance instance, @Nullable MatchmakerService matchmaker, @Nullable PlayerTrackerService playerTracker,
                          @NotNull EventNode<Event> eventNode, @NotNull ConfigProvider<GameModeConfig> configProvider) {
        this.matchmaker = matchmaker;
        this.playerTracker = playerTracker;
        this.configProvider = configProvider;
        this.npcHandler = new GameNpcHandler(configProvider, matchmaker, instance);

        this.registerListeners(eventNode);

        Collection<GameModeConfig> configs = configProvider.allConfigs();
        configProvider.addGlobalUpdateListener(this::handleUpdate);
        for (GameModeConfig config : configs) {
            if (!config.enabled()) continue;

            this.createDisplayItem(config, 0);
        }

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getInventory() == this.inventory) return;
            if (e.getPlayer().getOpenInventory() != this.inventory) return;
            e.setCancelled(true);
        });

        this.inventory.eventNode().addListener(InventoryPreClickEvent.class, this::handleInventoryClick);

        MinecraftServer.getSchedulerManager().buildTask(this::updatePlayerCounts)
                .repeat(2, ChronoUnit.SECONDS)
                .schedule();
    }

    private void handleInventoryClick(@NotNull InventoryPreClickEvent event) {
        Player player = event.getPlayer();
        int slot = event.getSlot();

        event.setCancelled(true);

        GameModeConfig config = this.slotToGameMode.get(slot);
        if (config == null) return; // clicked empty slot

        if (event.getClick() instanceof Click.Left) {
            QueueGameClickHandler.leftClick(player, config, this.matchmaker);
            player.closeInventory();
        }
        if (event.getClick() instanceof Click.Right) {
            QueueGameClickHandler.rightClick(player, config, this.matchmaker);
        }
    }

    private void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getHand() != PlayerHand.MAIN) return;

            Player player = event.getPlayer();
            if (player.getItemInMainHand().hasTag(LobbyEvents.SERVER_SELECTOR_TAG)) player.openInventory(this.inventory);
        });
    }

    private void handleUpdate(@NotNull ConfigUpdate<GameModeConfig> update) {
        switch (update) {
            case ConfigUpdate.Create(GameModeConfig newConfig) -> this.addGameMode(newConfig);
            case ConfigUpdate.Delete(GameModeConfig oldConfig) -> this.removeGameMode(oldConfig);
            case ConfigUpdate.Modify(GameModeConfig oldConfig, GameModeConfig newConfig) ->
                    this.updateGameMode(oldConfig, newConfig);
            default -> throw new IllegalStateException("Invalid config update: " + update);
        }
    }

    private void addGameMode(@NotNull GameModeConfig config) {
        if (!config.enabled()) return;

        // Display item
        this.createDisplayItem(config, 0);

        // NPCs
        this.npcHandler.renderNpcs();
    }

    private void removeGameMode(@NotNull GameModeConfig config) {
        ConfigItem item = config.displayItem();
        if (item != null) this.removeDisplayItem(item);

        this.npcHandler.renderNpcs();
    }

    private void updateGameMode(@NotNull GameModeConfig oldConfig, @NotNull GameModeConfig newConfig) {
        this.updateGameModeDisplayItem(newConfig, oldConfig.displayItem());

        // Re-render NPCs no matter what as game mode might have been disabled
        this.npcHandler.renderNpcs();
    }

    private void updateGameModeDisplayItem(@NotNull GameModeConfig newConfig, @Nullable ConfigItem oldItem) {
        if (oldItem != null) {
            // We always remove the old item, as we will re-add it if we have a new item, and we won't if we don't
            this.removeDisplayItem(oldItem);
        }

        this.createDisplayItem(newConfig, 0);
    }

    private void createDisplayItem(@NotNull GameModeConfig config, long playerCount) {
        ConfigItem item = config.displayItem();
        if (item == null) return; // If the item is null we have no item to create
        if (!config.enabled()) return; // If the config isn't enabled then we don't want to create an item

        List<Component> lore = ConfigItemConverter.createDisplayItemLore(item, config.maps() != null && !config.maps().isEmpty(), playerCount);
        ItemStack stack = ConfigItemConverter.convert(item, lore);
        if (stack == null) return;

        this.addDisplayItem(config, item, stack);
    }

    private void addDisplayItem(@NotNull GameModeConfig config, @NotNull ConfigItem item, @NotNull ItemStack stack) {
        this.slotToGameMode.put(item.slot(), config);
        this.inventory.setItemStack(item.slot(), stack);
    }

    private void removeDisplayItem(@NotNull ConfigItem item) {
        this.slotToGameMode.remove(item.slot());
        this.inventory.setItemStack(item.slot(), ItemStack.AIR);
    }

    private void updatePlayerCounts() {
        if (this.playerTracker == null) return;

        List<String> fleetNames = new ArrayList<>();
        for (GameModeConfig config : this.configProvider.allConfigs()) {
            if (!config.enabled()) continue;
            fleetNames.add(config.fleetName());
        }

        // GetFleetPlayerCounts has to have the fleet names set, and we may not have any game modes enabled
        if (fleetNames.isEmpty()) return;

        Map<String, Long> playerCounts;
        try {
            playerCounts = this.playerTracker.getFleetPlayerCounts(fleetNames);
        } catch (StatusRuntimeException exception) {
            LOGGER.error("Failed to get player counts for fleets", exception);
            return;
        }

        this.updatePlayerCounts(playerCounts);
    }

    private void updatePlayerCounts(@NotNull Map<String, Long> playerCounts) {
        for (Map.Entry<String, Long> entry : playerCounts.entrySet()) {
            this.npcHandler.updatePlayerCount(entry.getKey(), entry.getValue());

            for (GameModeConfig config : this.configProvider.allConfigs()) {
                if (!config.enabled()) continue;
                if (!config.fleetName().equals(entry.getKey())) continue;
                if (config.displayItem() == null) continue;

                this.updatePlayerCountInDisplayItem(config, entry.getValue());
            }
        }
    }

    private void updatePlayerCountInDisplayItem(@NotNull GameModeConfig config, long playerCount) {
        ConfigItem item = config.displayItem();
        if (item == null) return;

        boolean hasMaps = config.maps() != null && !config.maps().isEmpty();
        int slot = item.slot();

        ItemStack stack = this.inventory.getItemStack(slot);
        if (stack.isAir()) return;

        List<Component> newLore = ConfigItemConverter.createDisplayItemLore(item, hasMaps, playerCount);
        ItemStack newStack = stack.withLore(newLore);

        this.addDisplayItem(config, item, newStack);
    }
}
