package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.CustomModels;
import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.Random;
import java.util.function.Supplier;

public final class SpinnyCubeFeature implements LobbyFeature {
    private static final PlayerSkin EMORTAL_SKIN = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY0ODk0Njk5OTE3OSwKICAicHJvZmlsZUlkIiA6ICI3YmQ1YjQ1OTFlNmI0NzUzODI3NDFmYmQyZmU5YTRkNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJlbW9ydGFsZGV2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkxZDdhOWMwODBlZTdjYTZkZjlhYWJlM2I5NTliYWE4MThkYWUzYjQ1ZWI3YWRjMTMwZmYyNjU1YzlkOTRjY2YiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzNDBjMGUwM2RkMjRhMTFiMTVhOGIzM2MyYTdlOWUzMmFiYjIwNTFiMjQ4MWQwYmE3ZGVmZDYzNWNhN2E5MzMiCiAgICB9CiAgfQp9",
            "prWY0EtGdAfqt/nc4Vv/sMBcVEb6WMvaRumTk72e/NKe3dUPxmzlDRm6rw/mEE332JND6+sEI9PmDQ4jj/W41cn/XR/uZIBS+1qLE+57slEQA+ds+/kffKt1358JEV5/qyqCnODLVjwsRwazXJstC3eKNByaTQEyZ2jv/mFeAIAOF+0eQqDaaMGgxdIMRvWR8Nj6uIiBFTdCPIw3OYZ5bxqxm8Epr5PppF+sj7ZK0UyQ5/f1UoO9B/YMUt0OW+UlF3NYcMDs5mrg1N8Ajxvqbe8l0X7eWHgSYd0S/FopSCiVVQOQdZHRyhicmzLv6rE+xW03SB8NRfoaEEvSh8+QiLGMcyETeriwdDzdf8H9Iin3vDkVbMyRTAJ5jL/xDkoFDFR5HtNkrwYBRJoVkiWbnWyoBFofAjmmMmGmT5SFABS7I0iWLEoP4EMzqy84zDbpwOOioQz9UFlZV8AqmyEDv8Hx6px20zdR/jPr7tRQgqRhcPyzNsElcNLhkBfHmhpKffkrEPOAaal49rtB+3Jq+nX8Z1VyEZSW4MYnuq91bFZ1ciMzopYulPwP4cZkrGaqV84lxsqStI5+STi105KP4Bws+XDpop1eyPdDuVL/axq3VVkKeRqSoMv+xRYONGZJQgZ1t0WRIsXk7DLsjtx8QgffxqOwjW5CmZR0liSPfYM="
    );

    private static final ItemStack EMORTAL_HEAD = ItemStack.builder(Material.PLAYER_HEAD)
            .set(DataComponents.PROFILE, new ResolvableProfile(EMORTAL_SKIN))
            .build();
    private static final ItemStack CUBE = ItemStack.builder(Material.PHANTOM_MEMBRANE)
            .itemModel(CustomModels.CUBE.getModelId())
            .build();

    @Override
    public void register(@NotNull Instance instance) {
        // top 78
        // bottom 70
        Vec cubePos = new Vec(0.5, 74.5, -37.5);

        Entity cube = this.createCube();
        Entity emortalFace = this.createEmortalHead();

        instance.loadChunk(cubePos).thenRun(() -> {
            cube.setInstance(instance, cubePos);
            emortalFace.setInstance(instance, cubePos);
        });

        cube.scheduler().submitTask(new CubeEffectsTask(instance, cube));
    }

    private @NotNull Entity createCube() {
        BetterEntity entity = new BetterEntity(EntityType.ITEM_DISPLAY);
//        entity.setTicking(false);
        entity.setNoGravity(true);
        entity.setPhysics(false);

        ItemDisplayMeta meta = (ItemDisplayMeta) entity.getEntityMeta();
        meta.setHeight(4);
        meta.setWidth(4);
        meta.setScale(new Vec(2));
        meta.setBrightnessOverride(255);
        meta.setItemStack(CUBE);
        meta.setTransformationInterpolationDuration(5);

        return entity;
    }

    private @NotNull Entity createEmortalHead() {
        BetterEntity entity = new BetterEntity(EntityType.ITEM_DISPLAY);
        entity.setTicking(false);
        entity.setNoGravity(true);
        entity.setPhysics(false);

        ItemDisplayMeta meta = (ItemDisplayMeta) entity.getEntityMeta();
        meta.setScale(new Vec(1, 1, -1));
        meta.setHeight(2);
        meta.setWidth(2);
        meta.setBrightnessOverride(255);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setItemStack(EMORTAL_HEAD);

        return entity;
    }

    private static final class CubeEffectsTask implements Supplier<TaskSchedule> {
        private static final double RING_SPIN_SPEED = 1.5;
        private static final double RING_SIZE = 2.3;

        private static final Vec ROTATION_UNIT = new Vec(0, 1, 1).normalize();

        private final @NotNull Instance instance;
        private final @NotNull Entity cube;

        private final Quaternionf rotation = new Quaternionf(new AxisAngle4f(0, (float) ROTATION_UNIT.x(), (float) ROTATION_UNIT.y(), (float) ROTATION_UNIT.z()));
        private final Random random = new Random();

        private double time = 0;
        private int nextFizz = 0;
        private int nextSpark = 0;

        CubeEffectsTask(@NotNull Instance instance, @NotNull Entity cube) {
            this.instance = instance;
            this.cube = cube;
        }

        @Override
        public TaskSchedule get() {
            if (this.shouldPlayCubeSound()) {
                this.playCubeSound();
            }

            if (this.shouldFizz()) {
                this.fizz();
            }
            if (this.shouldSpark()) {
                this.spark();
            }

            this.createRingParticles();
            this.rotateCube();

            return TaskSchedule.tick(1);
        }

        private boolean shouldPlayCubeSound() {
            // Every 6 seconds
            return this.cube.getAliveTicks() % (20 * 6) == 0;
        }

        private void playCubeSound() {
            this.instance.playSound(Sound.sound(SoundEvent.BLOCK_BEACON_AMBIENT, Sound.Source.MASTER, 1.3f, 0.7f), this.cube.getPosition());
        }

        private boolean shouldFizz() {
            return this.nextFizz == this.cube.getAliveTicks();
        }

        private boolean shouldSpark() {
            return this.nextSpark == this.cube.getAliveTicks();
        }

        private void fizz() {
            // Fizz again between 3 and 10 seconds from now
            this.nextFizz += this.random.nextInt(20 * 3, 20 * 10);

            this.playFizzSound();
            this.spawnFizzParticles();
        }

        private void playFizzSound() {
            Sound sound = Sound.sound(SoundEvent.ENTITY_GENERIC_EXTINGUISH_FIRE, Sound.Source.MASTER, 0.3f, this.random.nextFloat() + 1.2f);
            this.instance.playSound(sound, this.cube.getPosition());
        }

        private void spawnFizzParticles() {
            for (int i = 0; i < 10; i++) {
                Vec direction = this.randomFizzDirection();

                ParticlePacket packet = new ParticlePacket(Particle.SPIT, this.cube.getPosition(), direction, 1, 0);
                this.cube.sendPacketToViewers(packet);
            }
        }

        private @NotNull Vec randomFizzDirection() {
            double randomX = this.random.nextDouble(-1F, 1F);
            double randomY = this.random.nextDouble(0.2F, 1F);
            double randomZ = this.random.nextDouble(-1F, 1F);
            return new Vec(randomX, randomY, randomZ).normalize();
        }

        private void spark() {
            // Spark again between 2 and 6 seconds from now
            this.nextSpark += this.random.nextInt(20 * 2, 20 * 6);

            this.playSparkSound();
            this.spawnSparkParticles(new Vec(random.nextDouble(-1, 2), 72, random.nextDouble(-39, -36)), new Vec(random.nextDouble(-1, 2), 77, random.nextDouble(-39, -36)));
        }

        private void playSparkSound() {
            Sound sound = Sound.sound(Key.key("random.cube.zap"), Sound.Source.MASTER, 0.1f, this.random.nextFloat() + 0.6f);
            this.instance.playSound(sound, this.cube.getPosition());
        }

        private void spawnSparkParticles(Vec start, Vec end) {
            double step = 1.0/20.0;
            for (double i = 0; i < 1.0; i += step) {
                Vec particlePos = lerpVec(start, end, i);

                ParticlePacket packet = new ParticlePacket(Particle.DUST.withProperties(NamedTextColor.AQUA, 0.6f),
                        particlePos, Pos.ZERO, 0, 1);

                instance.sendGroupedPacket(packet);
            }
        }

        private void createRingParticles() {
            double x = this.cube.getPosition().x();
            double z = this.cube.getPosition().z();

            Vec particleVec = this.calculateNewParticleVector();
            ParticlePacket packet = new ParticlePacket(Particle.END_ROD,
                    x + particleVec.x(), particleVec.y() + 74.5, z + particleVec.z(),
                    0f, 0f, 0f, 0,
                    1);
            this.cube.sendPacketToViewers(packet);
        }

        private @NotNull Vec calculateNewParticleVector() {
            return new Vec(Math.sin(this.time * RING_SPIN_SPEED) * RING_SIZE, 0, Math.cos(this.time * RING_SPIN_SPEED) * RING_SIZE)
                    .rotateAroundX(0.35)
                    .rotateAroundY(-this.time * 0.2);
        }

        private void rotateCube() {
            this.rotation.set(new AxisAngle4f((float) this.time, (float) ROTATION_UNIT.x(), (float) ROTATION_UNIT.y(), (float) ROTATION_UNIT.z()));

            float[] rotations = new float[] {
                    this.rotation.x(),
                    this.rotation.y(),
                    this.rotation.z(),
                    this.rotation.w()
            };
            this.time += 0.07;

            ItemDisplayMeta meta = (ItemDisplayMeta) this.cube.getEntityMeta();
            meta.setNotifyAboutChanges(false);
            meta.setLeftRotation(rotations);
            meta.setTransformationInterpolationStartDelta(0);
            meta.setNotifyAboutChanges(true);
        }

        private Vec lerpVec(Vec a, Vec b, double f) {
            return new Vec(
                    lerp(a.x(), b.x(), f),
                    lerp(a.y(), b.y(), f),
                    lerp(a.z(), b.z(), f)
            );
        }

        private double lerp(double a, double b, double f) {
            return a + f * (b - a);
        }
    }
}
