package dev.emortal.minestom.lobby.util.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InteractionEntity extends Entity {

    private final EventNode<InstanceEvent> eventNode;

    private @Nullable Consumer<Player> hitConsumer = null;

    public InteractionEntity() {
        super(EntityType.INTERACTION);

        this.eventNode = EventNode.type("npc-interact." + this.getEntityId(), EventFilter.INSTANCE);

        editEntityMeta(InteractionMeta.class, meta -> {
            meta.setWidth(1.5f);
            meta.setHeight(2f);
            meta.setResponse(true);
        });

        eventNode.addListener(EntityAttackEvent.class, e -> {
            if (e.getTarget() != this) return;
            if (!(e.getEntity() instanceof Player player)) return;
            if (hitConsumer != null) hitConsumer.accept(player);
        });
        eventNode.addListener(PlayerEntityInteractEvent.class, e -> {
            if (e.getTarget() != this) return;
            if (hitConsumer != null) hitConsumer.accept(e.getPlayer());
        });
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        instance.eventNode().addChild(this.eventNode);
        return super.setInstance(instance, spawnPosition);
    }

    @Override
    public void remove() {
        instance.eventNode().removeChild(this.eventNode);
        super.remove();
    }

    public void setHitConsumer(@Nullable Consumer<Player> hitConsumer) {
        this.hitConsumer = hitConsumer;
    }
}
