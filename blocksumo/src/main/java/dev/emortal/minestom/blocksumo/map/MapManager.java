package dev.emortal.minestom.blocksumo.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dev.emortal.minestom.blocksumo.utils.NoSaveyPolar;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(MapData.class, new MapData.Adapter()).create();

    private static final int CHUNK_LOADING_RADIUS = 5;

    private static final List<String> ENABLED_MAPS = List.of(
            "blocksumo",
            "castle",
            "end",
            "ice",
            "ruins",
            "deepdark"
    );
    private static final Path MAPS_PATH = Path.of("maps");

    private final @NotNull Map<String, PreLoadedMap> preLoadedMaps;

    public MapManager() {
        DimensionType overworld = MinecraftServer.getDimensionTypeRegistry().get(DimensionType.OVERWORLD);

        DimensionType dimensionType = DimensionType.builder()
                .timelines(overworld.timelines())
                .setAttribute(EnvironmentAttribute.CLOUD_COLOR, ShadowColor.fromHexString("#ffffffcc"))
                .setAttribute(EnvironmentAttribute.FOG_COLOR, new Color(0xc0d8ff))
                .setAttribute(EnvironmentAttribute.SKY_COLOR, new Color(0x78a7ff))
                .setAttribute(EnvironmentAttribute.CLOUD_HEIGHT, 110f)
                .build();
        DimensionType dimensionTypeFB = DimensionType.builder()
                .timelines(overworld.timelines())
                .setAttribute(EnvironmentAttribute.CLOUD_COLOR, ShadowColor.fromHexString("#ffffffcc"))
                .setAttribute(EnvironmentAttribute.FOG_COLOR, new Color(0xc0d8ff))
                .setAttribute(EnvironmentAttribute.SKY_COLOR, new Color(0x78a7ff))
                .setAttribute(EnvironmentAttribute.CLOUD_HEIGHT, 110f)
                .setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, NamedTextColor.WHITE)
                .ambientLight(1f)
                .build();

        MinecraftServer.getDimensionTypeRegistry().register("emortalmc:blocksumo", dimensionType);
        MinecraftServer.getDimensionTypeRegistry().register("emortalmc:blocksumofb", dimensionTypeFB);

        Map<String, PreLoadedMap> maps = new HashMap<>();
        for (String mapName : ENABLED_MAPS) {
            Path mapPath = MAPS_PATH.resolve(mapName);
            Path polarPath = mapPath.resolve("map.polar");
            Path dataPath = mapPath.resolve("map_data.json");

            try {
                MapData mapData = GSON.fromJson(new JsonReader(new FileReader(dataPath.toFile())), MapData.class);
                LOGGER.info("Loaded map data for map {}: [{}]", mapName, mapData);

                PolarLoader polarLoader = new NoSaveyPolar(polarPath);
//                if (!Files.exists(polarPath)) { // File needs to be converted
//                    PolarWorld world = AnvilPolar.anvilToPolar(mapPath, ChunkSelector.radius(CHUNK_LOADING_RADIUS));
//                    Files.write(polarPath, PolarWriter.write(world));
//                    polarLoader = new PolarLoader(world);
//                } else {
//                    polarLoader = new PolarLoader(polarPath);
//                }

                maps.put(mapName, new PreLoadedMap(polarLoader, mapData));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        this.preLoadedMaps = Map.copyOf(maps);
    }

    public @NotNull LoadedMap getMap(@Nullable String id) {
        LOGGER.info("Getting map {}", id);
        if (id == null) {
            return this.getRandomMap();
        }

        PreLoadedMap map = this.preLoadedMaps.get(id);
        if (map == null) {
            LOGGER.warn("Map {} not found, loading random map", id);
            return this.getRandomMap();
        }

        return map.load();
    }

    public @Nullable MapData getMapData(@NotNull String id) {
        PreLoadedMap map = this.preLoadedMaps.get(id);
        if (map == null) {
            LOGGER.warn("Map {} not ", id);
            return null;
        }

        return map.getMapData();
    }

    public @NotNull LoadedMap getRandomMap() {
        LOGGER.info("Getting random map");
        String randomMapId = ENABLED_MAPS.get(ThreadLocalRandom.current().nextInt(ENABLED_MAPS.size()));

        PreLoadedMap map = this.preLoadedMaps.get(randomMapId);
        return map.load();
    }

    private class PreLoadedMap {

        private final PolarLoader chunkLoader;
        private final MapData mapData;
        public PreLoadedMap(@NotNull PolarLoader chunkLoader, @NotNull MapData mapData) {
            this.chunkLoader = chunkLoader;
            this.mapData = mapData;
        }

        @NotNull LoadedMap load() {
            LOGGER.info("Creating new instance and loading chunks");
            DynamicRegistry<DimensionType> dimRegistry = MinecraftServer.getDimensionTypeRegistry();
            RegistryKey<DimensionType> dimensionType = dimRegistry.getKey(Key.key("emortalmc", "blocksumo"));
            InstanceContainer newInstance = MinecraftServer.getInstanceManager().createInstanceContainer(dimensionType);

            newInstance.setChunkLoader(this.chunkLoader);
            newInstance.enableAutoChunkLoad(false);

            for (int x = -CHUNK_LOADING_RADIUS; x < CHUNK_LOADING_RADIUS; x++) {
                for (int z = -CHUNK_LOADING_RADIUS; z < CHUNK_LOADING_RADIUS; z++) {
                    newInstance.loadChunk(x, z);
                }
            }

            newInstance.setChunkLoader(ChunkLoader.noop());

            return new LoadedMap(newInstance, this.mapData);
        }

        public MapData getMapData() {
            return mapData;
        }
    }
}