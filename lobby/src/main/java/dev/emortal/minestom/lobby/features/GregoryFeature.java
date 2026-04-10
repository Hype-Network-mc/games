package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class GregoryFeature implements LobbyFeature {

    @Override
    public void register(@NotNull Instance instance) {
        BetterEntity gregory = new BetterEntity(EntityType.SNIFFER);
        gregory.setPhysics(false);
        gregory.setTicking(false);

        gregory.setInstance(instance, new Pos(-18.5, 65, 9, 0f, 0f));

        instance.eventNode().addListener(PlayerEntityInteractEvent.class, e -> {
            if (e.getTarget() != gregory) return;
            ((SnifferMeta)e.getTarget().getEntityMeta()).setState(SnifferMeta.State.SNIFFING);
            e.getPlayer().playSound(Sound.sound(SoundEvent.ENTITY_SNIFFER_SNIFFING, Sound.Source.MASTER, 1f, 1f), e.getTarget().getPosition());
        });
    }
}
