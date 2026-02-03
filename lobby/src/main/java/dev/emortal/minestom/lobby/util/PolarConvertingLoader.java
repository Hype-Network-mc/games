package dev.emortal.minestom.lobby.util;

import net.hollowcube.polar.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class PolarConvertingLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolarConvertingLoader.class);

    private final @NotNull InstanceContainer instance;
    private final @NotNull String path;
    private final @NotNull ChunkSelector chunkSelector;

    public PolarConvertingLoader(@NotNull String path, @NotNull ChunkSelector chunkSelector) {
        this(MinecraftServer.getInstanceManager().createInstanceContainer(), path, chunkSelector);
    }

    public PolarConvertingLoader(@NotNull InstanceContainer instance, @NotNull String path, @NotNull ChunkSelector chunkSelector) {
        this.instance = instance;
        this.path = path;
        this.chunkSelector = chunkSelector;
    }

    public @NotNull CompletableFuture<InstanceContainer> load() {
        Path polarFile = Path.of(this.path + ".polar");
        Path anvilFile = Path.of(this.path);

        CompletableFuture<Void> loader;
        if (!Files.exists(polarFile)) {
            loader = this.convertFromAnvil(anvilFile, polarFile);
        } else {
            loader = this.loadFromPolar(polarFile);
        }

        return loader.thenApply(ignored -> this.instance);
    }

    private @NotNull CompletableFuture<Void> loadFromPolar(@NotNull Path file) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            return PolarLoader.streamLoad(this.instance, Channels.newChannel(new ByteArrayInputStream(bytes)), bytes.length, new PolarChainFix(), null, true);
        } catch (IOException exception) {
            LOGGER.error("Failed to load polar world from '{}'", file);
            throw new UncheckedIOException(exception);
        }
    }

    private @NotNull CompletableFuture<Void> convertFromAnvil(@NotNull Path anvilFile, @NotNull Path polarFile) {
        PolarWorld world;
        try {
            world = AnvilPolar.anvilToPolar(anvilFile, this.chunkSelector);
        } catch (IOException exception) {
            LOGGER.error("Failed to convert anvil world '{}' to polar", anvilFile);
            throw new UncheckedIOException(exception);
        }

        try {
            byte[] worldBytes = PolarWriter.write(world);
            Files.write(polarFile, worldBytes);
            return PolarLoader.streamLoad(instance, Channels.newChannel(new ByteArrayInputStream(worldBytes)), worldBytes.length, new PolarChainFix(), null, true);
        } catch (IOException exception) {
            LOGGER.error("Failed to save polar world to '{}'", polarFile);
            throw new UncheckedIOException(exception);
        }
    }
}
