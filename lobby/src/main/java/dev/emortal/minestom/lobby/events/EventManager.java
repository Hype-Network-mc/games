package dev.emortal.minestom.lobby.events;

import dev.emortal.api.message.party.EventDeleteMessage;
import dev.emortal.api.message.party.EventDisplayMessage;
import dev.emortal.api.message.party.EventStartMessage;
import dev.emortal.api.model.party.EventData;
import dev.emortal.api.service.party.PartyService;
import dev.emortal.api.utils.ProtoTimestampConverter;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class EventManager {

    private static final @NotNull Component EVENT_TITLE = Component.text("Event", NamedTextColor.GREEN, TextDecoration.BOLD);
    private static final @NotNull Component EVENT_SOON_SUBTITLE = Component.text("Starting soon!", NamedTextColor.GRAY);
    private static final @NotNull Component EVENT_RUNNING_SUBTITLE = Component.text("Click the NPC to join", NamedTextColor.GRAY);
    private static final @NotNull Component EVENT_STARTING_SUBTITLE = Component.text("The event has begun!", NamedTextColor.GRAY);
    private static final @NotNull Pos NPC_POSITION = new Pos(-3.5, 65, -4.5, -40, 0);

    private @Nullable EventNPC npc;

    public EventManager(
            @NotNull MessagingModule messagingModule,
            @NotNull PartyService partyService,
            @NotNull EventNode<PlayerEvent> eventNode,
            @NotNull Instance instance
    ) {
        this.getDisplayedEvent(partyService).ifPresent(event -> this.updateNpc(partyService, event, instance));

        messagingModule.addListener(EventDisplayMessage.class, message -> {
            this.updateNpc(partyService, message.getEvent(), instance);
            Title title = Title.title(EVENT_TITLE, EVENT_SOON_SUBTITLE);
            Audiences.all().showTitle(title);
        });
        messagingModule.addListener(EventStartMessage.class, message -> {
            this.updateNpc(partyService, message.getEvent(), instance);
            Title title = Title.title(EVENT_TITLE, EVENT_STARTING_SUBTITLE);
            Audiences.all().showTitle(title);
        });
        messagingModule.addListener(EventDeleteMessage.class, ignored -> {
            if (this.npc == null) return;
            this.npc.remove();
            this.npc = null;
        });

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            if (this.npc == null) return;
            Title title = Title.title(
                    EVENT_TITLE,
                    this.npc.getEvent().hasPartyId() ? EVENT_RUNNING_SUBTITLE : EVENT_SOON_SUBTITLE
            );
            event.getPlayer().showTitle(title);
        });

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (this.npc == null) return;
            this.npc.update();
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private @NotNull Optional<EventData> getDisplayedEvent(@NotNull PartyService partyService) {
        List<EventData> events = partyService.listEvents();

        // check if there is an event that is currently running
        Optional<EventData> optionalEvent = events.stream()
                .filter(EventData::hasPartyId)
                .findAny();
        if (optionalEvent.isPresent()) return optionalEvent;

        // find the event that is starting the earliest
        Instant now = Instant.now();
        return events.stream()
                .filter(event -> ProtoTimestampConverter.fromProto(event.getDisplayTime()).isBefore(now))
                .min(Comparator.comparing(event -> ProtoTimestampConverter.fromProto(event.getStartTime())));
    }

    private void updateNpc(@NotNull PartyService partyService, @NotNull EventData event, @NotNull Instance instance) {
        if (this.npc == null) {
            this.npc = new EventNPC(partyService, event);
            this.npc.setInstance(instance, NPC_POSITION);
            return;
        }
        this.npc.setEvent(event);
    }

}
