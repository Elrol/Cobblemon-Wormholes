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
            Codec.FLOAT.fieldOf("wormholeScale").forGetter(data -> data.wormholeScale),
            DimensionConfig.CODEC.fieldOf("dimensions").forGetter(data -> data.dimensions)
    ).apply(instance, (isDebug, wormholeScale, dimension) -> {
        WormholeConfig data = new WormholeConfig();

        data.isDebug = isDebug;
        data.wormholeScale = wormholeScale;
        data.dimensions = dimension;

        return data;
    }));

    private static final String fileName = "config.json";

    public boolean isDebug = false;
    public float wormholeScale = 5.0f;

    public DimensionConfig dimensions = new DimensionConfig();


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

}