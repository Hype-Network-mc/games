package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class RandomPowerUpHandler {

    private final @NotNull BlockSumoGame game;
    private final @NotNull PowerUpManager powerUpManager;

    public RandomPowerUpHandler(@NotNull BlockSumoGame game, @NotNull PowerUpManager powerUpManager) {
        this.game = game;
        this.powerUpManager = powerUpManager;
    }

    public void startRandomPowerUpTasks() {
        Instance instance = game.getInstance();

        instance.scheduler()
                .buildTask(() -> this.powerUpManager.spawnPowerUp(this.powerUpManager.findRandomPowerUp(SpawnLocation.CENTER)))
                .delay(TaskSchedule.seconds(10))
                .repeat(TaskSchedule.seconds(30))
                .schedule();

        instance.scheduler()
                .buildTask(() -> this.powerUpManager.givePowerUpToAll(this.powerUpManager.findRandomPowerUp(SpawnLocation.ANYWHERE)))
                .delay(TaskSchedule.seconds(5))
                .repeat(TaskSchedule.seconds(45))
                .schedule();
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {

    }
}
