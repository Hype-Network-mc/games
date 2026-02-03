package dev.emortal.minestom.lobby.gadget;

import dev.emortal.minestom.lobby.util.WorldBlock;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BlockChangingGadget extends Gadget {

    protected BlockChangingGadget(@NotNull String name, @NotNull ItemStack item, @NotNull String permissionName, boolean projectile) {
        super(name, item, permissionName, projectile);
    }

    protected void regenerateInstanceBlocks(@NotNull Instance instance, @NotNull List<WorldBlock> blocks) {
        this.regenerateBlocks(instance, blocks, block -> instance.setBlock(block.position(), block.block()));
    }

    protected void regenerateBlocks(@NotNull Instance instance, @NotNull List<WorldBlock> blocks, @NotNull Consumer<WorldBlock> action) {
        instance.scheduler().submitTask(new BlockRegenerationTask(blocks, action));
    }

    private static final class BlockRegenerationTask implements Supplier<TaskSchedule> {

        private final @NotNull List<WorldBlock> blocks;
        private final @NotNull Consumer<WorldBlock> action;

        private boolean firstRun = true;
        private int regeneratedCount = 0;

        BlockRegenerationTask(@NotNull List<WorldBlock> blocks, @NotNull Consumer<WorldBlock> action) {
            this.blocks = blocks;
            this.action = action;
        }

        @Override
        public TaskSchedule get() {
            if (this.firstRun) {
                this.firstRun = false;
                return TaskSchedule.seconds(3);
            }

            for (int i = 0; i < 5; i++) {
                if (this.regeneratedCount >= this.blocks.size()) return TaskSchedule.stop();

                WorldBlock block = this.blocks.get(this.regeneratedCount);
                this.action.accept(block);

                this.regeneratedCount++;
            }

            return TaskSchedule.nextTick();
        }
    }
}
