package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.game.BattleGame;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.utils.time.TimeUnit;

public class ItemListener {

    public ItemListener(BattleGame game) {
        game.getEventNode().addListener(ItemDropEvent.class, e -> {
            ItemEntity itemEntity = new ItemEntity(e.getItemStack());
            itemEntity.setPickupDelay(40, TimeUnit.SERVER_TICK);
            Vec velocity = e.getEntity().getPosition().direction().mul(6);
            itemEntity.setVelocity(velocity);
            itemEntity.setInstance(e.getEntity().getInstance(), e.getEntity().getPosition().add(0, e.getEntity().getEyeHeight(), 0));
        });

        game.getEventNode().addListener(PickupItemEvent.class, e -> {
            if (!(e.getLivingEntity() instanceof Player player)) return;

            boolean success = player.getInventory().addItemStack(e.getItemStack());
            e.setCancelled(!success);
        });
    }

}
