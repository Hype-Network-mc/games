package dev.emortal.minestom.lobby.matchmaking;

import dev.emortal.api.liveconfigparser.configs.gamemode.GameModeConfig;
import dev.emortal.api.model.matchmaker.PendingMatch;
import dev.emortal.api.model.matchmaker.Ticket;
import dev.emortal.api.service.matchmaker.DequeuePlayerResult;
import dev.emortal.api.service.matchmaker.MatchmakerService;
import dev.emortal.api.utils.GrpcStubCollection;
import dev.emortal.api.utils.ProtoTimestampConverter;
import dev.emortal.minestom.core.module.matchmaker.session.MatchmakingSession;
import dev.emortal.minestom.lobby.game.MapSelectorInventory;
import io.grpc.StatusRuntimeException;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created when a player queues for matchmaking.
 * This stores data suck as the boss bar and tracks teleport time.
 */
public final class LobbyMatchmakingSession extends MatchmakingSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyMatchmakingSession.class);

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final ItemStack LEAVE_QUEUE_ITEM = ItemStack.builder(Material.BARRIER)
            .set(DataComponents.ITEM_NAME, Component.text("Leave Queue", NamedTextColor.RED))
            .set(DataComponents.LORE, List.of(Component.empty(), Component.text("Right-click to leave the queue", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)))
            .build();
    private static final ItemStack MAP_SELECTOR_ITEM = ItemStack.builder(Material.CHEST)
            .set(DataComponents.ITEM_NAME, Component.text("Map Selector", NamedTextColor.GOLD))
            .set(DataComponents.LORE, List.of(Component.empty(), Component.text("Right-click to open the map selector", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)))
            .build();

    private static final char[] SPINNER = {'▘', '▖'};
    private static final @NotNull String IN_QUEUE = "<spinner> <gradient:gold:white:%s>In Queue For <gradient:aqua:blue><bold><game><reset> <spinner>";
    private static final @NotNull String COUNTDOWN = "<gradient:#ff9eed:#ff956b:%s>Teleporting to <gradient:aqua:blue><bold><game></bold></gradient> <white>(<player_count>/<max_players>)</white> in <white><time></white> seconds!";
    private static final @NotNull String TELEPORTING = "<green>Teleporting to <gradient:aqua:blue><bold><game></bold></gradient>...";

    private static final long SWITCH_TO_COUNTDOWN_MILLIS = 0;

    private final MatchmakerService matchmaker;

    private final AtomicInteger ticksAlive = new AtomicInteger(0);
    private final AtomicBoolean playedTeleportSound = new AtomicBoolean(false);
    private final Player player;
    private final Ticket ticket;
    private final GameModeConfig gameMode;

    private final Task tickTask;
    private final EventNode<PlayerEvent> eventNode;

    private State state = State.IN_QUEUE;
    private final BossBar bossBar;
    private @Nullable PendingMatch pendingMatch;
    private @Nullable Instant teleportTime;
    private @Nullable Task teleportTask; // cancels the bossbar if the player does not leave within 10 seconds.

    public LobbyMatchmakingSession(@NotNull Player player, @NotNull GameModeConfig gameMode, @NotNull Ticket ticket, @Nullable PendingMatch pendingMatch) {
        super(player, ticket);

        this.player = player;
        this.ticket = ticket;
        this.gameMode = gameMode;
        this.matchmaker = GrpcStubCollection.getMatchmakerService().orElse(null);
        this.pendingMatch = pendingMatch;

        this.bossBar = BossBar.bossBar(Component.empty(), 0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        player.showBossBar(this.bossBar);

        if (pendingMatch == null) this.changeToInQueue();
        else this.onPendingMatchCreate(pendingMatch);

        player.getInventory().setItemStack(8, LEAVE_QUEUE_ITEM);

        this.tickTask = MinecraftServer.getSchedulerManager().buildTask(this::tick).repeat(1, TimeUnit.CLIENT_TICK).schedule();

        // todo unregister event node and ticket on logout
        this.eventNode = EventNode.event("mm-%s-%s".formatted(player.getUuid(), UUID.randomUUID()), EventFilter.PLAYER, event -> event.getPlayer() == player);
        MinecraftServer.getGlobalEventHandler().addChild(this.eventNode);

        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (item == LEAVE_QUEUE_ITEM) {
                Thread.startVirtualThread(this::useCancelItem);
            } else if (item == MAP_SELECTOR_ITEM) {
                this.useMapSelectorItem();
            }
        });
        this.eventNode.addListener(PlayerUseItemOnBlockEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (item == LEAVE_QUEUE_ITEM) {
                Thread.startVirtualThread(this::useCancelItem);
            } else if (item == MAP_SELECTOR_ITEM) {
                this.useMapSelectorItem();
            }
        });

        // Map selector item
        if (gameMode.maps() != null && !gameMode.maps().isEmpty()) {
            player.getInventory().setItemStack(7, MAP_SELECTOR_ITEM);
        }
    }

    @Override
    public void onPendingMatchCancelled(@NotNull PendingMatch pendingMatch) {
        this.state = State.IN_QUEUE;
        this.teleportTime = null;
        this.playedTeleportSound.set(false);
        this.bossBar.name(this.createInQueueName());

        if (this.teleportTask != null) {
            this.teleportTask.cancel();
            this.teleportTask = null;
        }

        this.pendingMatch = null;
    }

    @Override
    public void onPendingMatchUpdate(@NotNull PendingMatch pendingMatch) {
        this.pendingMatch = pendingMatch;
    }

    @Override
    public void onPendingMatchCreate(@NotNull PendingMatch pendingMatch) {
        this.pendingMatch = pendingMatch;
        if (this.teleportTime != null) return;

        this.teleportTime = ProtoTimestampConverter.fromProto(pendingMatch.getTeleportTime());
        long millisUntil = (int) (this.teleportTime.toEpochMilli() - System.currentTimeMillis());

        if (millisUntil <= SWITCH_TO_COUNTDOWN_MILLIS) {
            this.changeToTeleporting();
        } else {
            this.state = State.COUNTDOWN;
            this.player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1f, 1.5f));
        }
    }

    @Override
    public void notifyDeletion(@NotNull DeleteReason reason) {
        switch (reason) {
            case MANUAL_DEQUEUE ->
                    this.player.sendMessage(Component.text("You have been removed from the queue.", NamedTextColor.RED));
            case GAME_MODE_DELETED ->
                    this.player.sendMessage(Component.text("The game mode you were in queue for has been disabled.", NamedTextColor.RED));
            case MATCH_CREATED, UNKNOWN -> {
            } // do nothing
        }
    }

    private void tick() {
        this.ticksAlive.incrementAndGet();

        switch (this.state) {
            case IN_QUEUE -> this.bossBar.name(this.createInQueueName());
            case COUNTDOWN -> {
                long millisRemaining = this.teleportTime.toEpochMilli() - System.currentTimeMillis();
                if (millisRemaining <= SWITCH_TO_COUNTDOWN_MILLIS) {
                    this.changeToTeleporting();
                    return;
                }
                this.bossBar.name(this.createCountdownName());
            }
            default -> {
            } // do nothing
        }
    }

    private void changeToInQueue() {
        this.state = State.IN_QUEUE;
        this.bossBar.name(this.createInQueueName());
        this.player.playSound(Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 1f, 1f));
    }

    private void changeToTeleporting() {
        this.state = State.TELEPORTING;
        this.bossBar.name(this.createTeleportingName());
        this.player.playSound(Sound.sound(SoundEvent.BLOCK_BEACON_POWER_SELECT, Sound.Source.MASTER, 1f, 1.5f));
        this.playedTeleportSound.set(true);
    }

    @Blocking
    private void useCancelItem() {
        DequeuePlayerResult result;
        try {
            result = this.matchmaker.dequeuePlayer(this.player.getUuid());
        } catch (StatusRuntimeException exception) {
            LOGGER.error("Failed to dequeue '{}'", this.player.getUsername(), exception);
            this.player.sendMessage(Component.text("An unknown error occurred", NamedTextColor.RED));
            return;
        }

        switch (result) {
            case NO_PERMISSION ->
                    this.player.sendMessage(Component.text("You do not have permission to dequeue your party.", NamedTextColor.RED));
            case ALREADY_MARKED_FOR_DEQUEUE, SUCCESS -> {} // ignore
            default -> {
                LOGGER.error("Failed to dequeue '{}' result '{}'", this.player.getUsername(), result);
                this.player.sendMessage(Component.text("An unknown error occurred", NamedTextColor.RED));
            }
        }
    }

    private void useMapSelectorItem() {
        MapSelectorInventory selectorInventory = new MapSelectorInventory(this.gameMode, this.matchmaker, true);
        this.player.openInventory(selectorInventory);
    }

    /**
     * Called by the MatchmakingManager when a ticket is cancelled.
     * The ticket is already deleted at this point, so it is safe to destroy.
     */
    public void destroy() {
        // TODO: test and PROFILE this is everything that needs to be cleaned up
        this.tickTask.cancel();
        if (this.teleportTask != null) {
            this.teleportTask.cancel();
        }
        this.player.hideBossBar(this.bossBar);

        EventNode<? super PlayerEvent> parent = this.eventNode.getParent();
        if (parent != null) {
            parent.removeChild(this.eventNode);
        }

        this.player.getInventory().setItemStack(8, ItemStack.AIR);
        this.player.getInventory().setItemStack(7, ItemStack.AIR);
    }

    private @NotNull Component createInQueueName() {
        double gradient = (((this.ticksAlive.get() * 0.05) % 2) - 1.0);
        String spinner = String.valueOf(getSpinnerChar());
        String miniValue = IN_QUEUE.formatted(gradient);

        return MINI_MESSAGE.deserialize(miniValue, Placeholder.unparsed("spinner", spinner), Placeholder.unparsed("game", this.gameMode.friendlyName()));
    }

    private @NotNull Component createCountdownName() {
        double gradient = (((this.ticksAlive.get() * 0.05) % 2) - 1.0);
        String miniValue = COUNTDOWN.formatted(gradient);

        long millisToTeleport = this.teleportTime.toEpochMilli() - System.currentTimeMillis();
        int secondsToTeleport = (int) Math.ceil(millisToTeleport / 1000.0); // Always round up to prevent teleporting in 0 seconds

        return MINI_MESSAGE.deserialize(miniValue,
                Placeholder.unparsed("game", this.gameMode.friendlyName()),
                Placeholder.unparsed("time", String.valueOf(secondsToTeleport)),
                Placeholder.unparsed("player_count", String.valueOf(this.pendingMatch.getPlayerCount())),
                Placeholder.unparsed("max_players", String.valueOf(this.gameMode.maxPlayers()))
        );
    }

    private @NotNull Component createTeleportingName() {
        return MINI_MESSAGE.deserialize(TELEPORTING, Placeholder.unparsed("game", this.gameMode.friendlyName()));
    }

    private char getSpinnerChar() {
        int tick = this.ticksAlive.get();
        return SPINNER[((int) (Math.floor(tick / 10.0) % SPINNER.length))];
    }

    private enum State {
        IN_QUEUE,
        COUNTDOWN,
        TELEPORTING
    }
}
