package dev.emortal.minestom.lobby.util.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class BetterLivingEntity extends LivingEntity {

    private boolean ticking = true;

    public BetterLivingEntity(@NotNull EntityType entityType) {
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
}
