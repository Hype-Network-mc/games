package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public final class RavenousFeature implements LobbyFeature {

    @Override
    public void register(@NotNull Instance instance) {
        instance.setBlock(40, 65, -24, Block.AIR);

        BetterEntity cat = new BetterEntity(EntityType.CAT);
        CatMeta meta = (CatMeta) cat.getEntityMeta();
        cat.setPhysics(false);
        cat.setTicking(false);
        meta.setBaby(true);

        cat.setInstance(instance, new Pos(40.5, 65, -23.5, 0f, 65f));

        BetterEntity steak = new BetterEntity(EntityType.ITEM_DISPLAY);
        ItemDisplayMeta steakMeta = (ItemDisplayMeta) steak.getEntityMeta();
        steak.setPhysics(false);
        steak.setTicking(false);
        steakMeta.setItemStack(ItemStack.of(Material.BEEF));
        steakMeta.setScale(new Vec(0.8));

        steak.setInstance(instance, new Pos(40.5, 65, -23.1, 0f, 90f));
    }
}
