package dev.emortal.minestom.lobby.gadget.lobber;

import dev.emortal.minestom.lobby.LobbyTags;
import dev.emortal.minestom.lobby.util.entity.BetterEntityProjectile;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class BlockLobberEntity extends BetterEntityProjectile {

    public BlockLobberEntity(@NotNull Entity shooter, @NotNull Block block) {
        super(shooter, EntityType.FALLING_BLOCK);

//        setAerodynamics(getAerodynamics().withGravity(-0.04).withAirResistance(0.0, 0.0));

        FallingBlockMeta meta = (FallingBlockMeta) super.entityMeta;
        super.setTag(LobbyTags.LOBBABLE, true);

        meta.setBlock(block);
        super.scheduleRemove(5 * 20, TimeUnit.SERVER_TICK);
    }

    @Override
    public void tick(long time) {
        if (super.instance == null || super.isRemoved() || !ChunkUtils.isLoaded(super.currentChunk)) return;

        super.tick(time);

        if (super.onGround && instance != null) {
            EventDispatcher.call(new ProjectileCollideWithBlockEvent(this, super.position, super.instance.getBlock(super.position.sub(0, 1, 0))));
        }
    }
}
