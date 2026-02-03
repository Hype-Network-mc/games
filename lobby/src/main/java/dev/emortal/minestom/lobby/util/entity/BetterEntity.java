package dev.emortal.minestom.lobby.util.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BetterEntity extends Entity {

    private boolean ticking = true;

    public BetterEntity(@NotNull EntityType entityType) {
        super(entityType);
    }

    @Override
    public void tick(long time) {
        if (this.ticking) super.tick(time);
    }

    public void setPhysics(boolean physics) {
        this.hasPhysics = physics;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        sendPacketToViewers(getPassengersPacket());
    }
}
