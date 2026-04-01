package dev.emortal.minestom.lobby.events;

import dev.emortal.api.model.party.EventData;
import dev.emortal.api.service.party.JoinPartyResult;
import dev.emortal.api.service.party.PartyService;
import dev.emortal.api.utils.ProtoTimestampConverter;
import dev.emortal.minestom.lobby.util.DurationFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.click.ClickType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class EventNPC extends Entity {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(EventManager.class);

    private final @NotNull Entity eventNameTag = new Entity(EntityType.TEXT_DISPLAY);
    private final @NotNull Entity startingInTag = new Entity(EntityType.TEXT_DISPLAY);
    private final @NotNull Entity statusTag = new Entity(EntityType.TEXT_DISPLAY);

    private final @NotNull PartyService partyService;
    private @NotNull EventData event;

    public EventNPC(@NotNull PartyService partyService, @NotNull EventData event) {
        super(EntityType.CAT);

        this.partyService = partyService;
        this.event = event;
        this.setEvent(event);

        this.eventNameTag.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            meta.setBackgroundColor(0);
        });
        this.eventNameTag.setNoGravity(true);

        this.startingInTag.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            meta.setText(Component.text("Starting in..."));
            meta.setScale(new Vec(0.75));
            meta.setBackgroundColor(0);
        });
        this.startingInTag.setNoGravity(true);

        this.statusTag.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            meta.setBackgroundColor(0);
        });
        this.statusTag.setNoGravity(true);


        eventNode().addListener(PlayerEntityInteractEvent.class, e -> {
            if (e.getHand() != PlayerHand.MAIN) return;
            Player player = e.getPlayer();
            if (e.getTarget() != this) return;

            this.interact(player, ClickType.RIGHT_CLICK);
        });
        eventNode().addListener(EntityAttackEvent.class, e -> {
            if (!(e.getEntity() instanceof Player player)) return;
            if (e.getTarget() != this) return;

            this.interact(player, ClickType.LEFT_CLICK);
        });
    }

    private void interact(@NotNull Player player, @NotNull ClickType clickType) {
        if (!this.event.hasPartyId()) return;
        UUID ownerId = UUID.fromString(this.event.getOwnerId());
        Thread.startVirtualThread(() -> {
            JoinPartyResult result = this.partyService.joinParty(player.getUuid(), player.getUsername(), ownerId);
            if (result instanceof JoinPartyResult.Error error) {
                if (error == JoinPartyResult.Error.NOT_INVITED) {
                    player.sendMessage(Component.text("Failed to join event", NamedTextColor.RED));
                    LOGGER.error("player {} was not invited to event {}", player.getUuid(), event.getId());
                }
                return;
            }
            player.sendMessage(Component.text("Joined event", NamedTextColor.GREEN));
        });
    }

    public void setEvent(@NotNull EventData event) {
        this.event = event;
        this.update();
    }

    public void update() {
//        PlayerSkin currentSkin = this.getSkin();
//        if (currentSkin != null) {
//            PlayerSkin newSkin = ProtoConverter.convert(this.event.getOwnerSkin());
//            if (!currentSkin.equals(newSkin)) this.setSkin(newSkin);
//        }

        this.eventNameTag.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(Component.text(event.getOwnerUsername() + "'s Event")));

        if (this.event.hasPartyId()) {
            // hide the "starting in" tag
            this.hideStartingInTag();

            // show the "click to join" tag
            this.statusTag.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(Component.text("Click to join")));
        } else {
            Instant startTime = ProtoTimestampConverter.fromProto(this.event.getStartTime());
            Duration duration = Duration.between(Instant.now(), startTime);

            // check if the event is starting
            if (!DurationFormatter.canBeFormatted(duration)) {
                // if so, hide the "starting in" tag
                this.hideStartingInTag();

                // and show that the event is starting
                this.statusTag.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(Component.text("Event starting")));
                return;
            }
            // otherwise, update the time

            // show the "starting in" tag
            if (!this.startingInTag.isAutoViewable()) this.startingInTag.setAutoViewable(true);

            // show the starting time
            String formatted = DurationFormatter.format(duration, 2);
            this.statusTag.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(Component.text(formatted)));
        }
    }

    private void hideStartingInTag() {
        if (this.startingInTag.isAutoViewable()) {
            this.startingInTag.setAutoViewable(false);
            new HashSet<>(this.startingInTag.getViewers()).forEach(this.startingInTag::removeViewer);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        Point tagPosition = spawnPosition.add(0, 2, 0);
        return CompletableFuture.allOf(
                super.setInstance(instance, spawnPosition),
                this.eventNameTag.setInstance(instance, tagPosition.add(0, 0.6, 0)),
                this.startingInTag.setInstance(instance, tagPosition.add(0, 0.3, 0)),
                this.statusTag.setInstance(instance, tagPosition)
        );
    }

    @Override
    protected void remove(boolean permanent) {
        super.remove(permanent);
        this.eventNameTag.remove();
        this.startingInTag.remove();
        this.statusTag.remove();
    }

    public @NotNull EventData getEvent() {
        return event;
    }

}
