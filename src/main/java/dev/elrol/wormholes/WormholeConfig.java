package dev.elrol.wormholes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.wormholes.libs.JsonUtils;
import dev.elrol.wormholes.libs.WormholeConstants;

public class WormholeConfig {

    public static final Codec<WormholeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("isDebug").forGetter(data -> data.isDebug),
            DimensionConfig.CODEC.fieldOf("dimensions").forGetter(data -> data.dimensions),
            EntityConfig.CODEC.fieldOf("entity").forGetter(data -> data.entity)
    ).apply(instance, (isDebug, dimension, entity) -> {
        WormholeConfig data = new WormholeConfig();

        data.isDebug = isDebug;
        data.dimensions = dimension;
        data.entity = entity;

        return data;
    }));

    private static final String fileName = "config.json";

    public boolean isDebug = false;

    public DimensionConfig dimensions = new DimensionConfig();
    public EntityConfig entity = new EntityConfig();


    public void save() {
        DataResult<JsonElement> jsonResult = CODEC.encodeStart(JsonOps.INSTANCE, this);
        JsonUtils.saveToJson(WormholeConstants.configDir, fileName, jsonResult.getOrThrow());
    }

    public static WormholeConfig load() {
        JsonElement json = JsonUtils.loadFromJson(WormholeConstants.configDir, fileName, JsonParser.parseString("{}"));
        DataResult<Pair<WormholeConfig, JsonElement>> configPair = CODEC.decode(JsonOps.INSTANCE, json);

        if(configPair.isError()) {
            WormholeConfig newConfig = new WormholeConfig();
            newConfig.save();
            return newConfig;
        }

        return configPair.getOrThrow().getFirst();
    }

    public static class DimensionConfig {

        public static final Codec<DimensionConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("chunksBetweenCells").forGetter(DimensionConfig::getChunksBetweenCells),
                Codec.INT.fieldOf("cellSize").forGetter(DimensionConfig::getCellSize),
                Codec.INT.fieldOf("cellElevation").forGetter(DimensionConfig::getCellElevation)
        ).apply(instance, (chunksBetweenCells, cellSize, cellElevation) -> {
            DimensionConfig data = new DimensionConfig();
            data.chunksBetweenCells = chunksBetweenCells;
            data.cellSize = cellSize;
            data.cellElevation = cellElevation;
            return data;
        }));

        private int chunksBetweenCells = 8;
        private int cellSize = 200;
        private int cellElevation = 64;

        public int getChunksBetweenCells() { return chunksBetweenCells; }
        public int getCellSize() { return cellSize; }
        public int getCellElevation() { return cellElevation; }
    }

    public static class EntityConfig {

        public static final Codec<EntityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("wormholeScale").forGetter(EntityConfig::getWormholeScale),
                Codec.FLOAT.fieldOf("wormholeSpawnChange").forGetter(EntityConfig::getWormholeSpawnChance),
                Codec.INT.fieldOf("minSpawnRadius").forGetter(EntityConfig::getMinSpawnRadius),
                Codec.INT.fieldOf("maxSpawnRadius").forGetter(EntityConfig::getMaxSpawnRadius),
                Codec.INT.fieldOf("minSpawnInterval").forGetter(EntityConfig::getMinSpawnInterval),
                Codec.INT.fieldOf("maxSpawnInterval").forGetter(EntityConfig::getMaxSpawnInterval),
                Codec.INT.fieldOf("spawnHeight").forGetter(EntityConfig::getSpawnHeight)
        ).apply(instance, (wormholeScale, wormholeSpawnRate, minSpawnRadius, maxSpawnRadius, minSpawnInterval, maxSpawnInterval, spawnHeight) -> {
            EntityConfig data = new EntityConfig();
            data.wormholeScale = wormholeScale;
            data.wormholeSpawnChance = wormholeSpawnRate;
            data.minSpawnRadius = minSpawnRadius;
            data.maxSpawnRadius = maxSpawnRadius;
            data.minSpawnInterval = minSpawnInterval;
            data.maxSpawnInterval = maxSpawnInterval;
            data.spawnHeight = spawnHeight;
            return data;
        }));

        public float wormholeScale = 5.0f;
        public float wormholeSpawnChance = 0.5f;
        public int minSpawnRadius = 16;
        public int maxSpawnRadius = 24;
        public int minSpawnInterval = 5;
        public int maxSpawnInterval = 10;
        public int spawnHeight = 4;

        public float getWormholeScale() {
            return wormholeScale;
        }

        public float getWormholeSpawnChance() {
            return wormholeSpawnChance;
        }

        public int getMinSpawnRadius() {
            return minSpawnRadius;
        }

        public int getMaxSpawnRadius() {
            return maxSpawnRadius;
        }

        public int getMinSpawnInterval() {
            return minSpawnInterval;
        }

        public int getMaxSpawnInterval() {
            return maxSpawnInterval;
        }

        public int getSpawnHeight() {
            return spawnHeight;
        }

        public int getWormholeSize() {
            return Math.round(wormholeScale * 5);
        }

        public int getWormholeRadius() {
            return Math.round((getWormholeSize() - 1.0f) / 2.0f);
        }
    }
}