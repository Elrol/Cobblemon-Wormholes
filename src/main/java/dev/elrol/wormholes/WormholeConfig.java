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
            Codec.BOOL.fieldOf("isDebug").forGetter(data -> data.isDebug)
    ).apply(instance, (isDebug) -> {
        WormholeConfig data = new WormholeConfig();

        data.isDebug = isDebug;

        return data;
    }));

    private static final String fileName = "config.json";

    public boolean isDebug = false;

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

}