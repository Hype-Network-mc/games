package dev.emortal.minestom.lobby.util.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BetterEntityProjectile extends EntityProjectile {

    public boolean ticking = true;

    public BetterEntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
        super(shooter, entityType);
    }

    @Override
    public void tick(long time) {
        if (this.ticking) super.tick(time);
    }

    // Fix error spam because dumb isStuck function
    @Override
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks, int flags, boolean shouldConfirm) {
        if (instance == null) return CompletableFuture.completedFuture(null);
        return super.teleport(position, chunks, flags, shouldConfirm);
    }

    public void setPhysics(boolean physics) {
        this.hasPhysics = physics;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }
}
