package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.CustomModels;
import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public final class ModelDecorationFeature implements LobbyFeature {

    @Override
    public void register(@NotNull Instance instance) {
        spawnModel(CustomModels.STREET_SIGN, new Vec(1.2), instance, new Pos(-10.5, 65.7, -6.5, -20, 0));
    }

    private void spawnModel(CustomModels customModel, Vec scale, Instance instance, Pos pos) {
        BetterEntity modelEntity = new BetterEntity(EntityType.ITEM_DISPLAY);
        modelEntity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setScale(scale);
            meta.setItemStack(ItemStack.builder(Material.PHANTOM_MEMBRANE).itemModel(customModel.getModelId()).build());
        });
        modelEntity.setTicking(false);
        modelEntity.setInstance(instance, pos);
    }

}
