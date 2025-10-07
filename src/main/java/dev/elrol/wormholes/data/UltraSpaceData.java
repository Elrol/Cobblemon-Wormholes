package dev.elrol.wormholes.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.libs.JsonUtils;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public class UltraSpaceData {

    public static final Codec<UltraSpaceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(data -> data.x),
            Codec.INT.fieldOf("z").forGetter(data -> data.z)
    ).apply(instance, (x, z) -> {
        UltraSpaceData data = new UltraSpaceData();
        data.x = x;
        data.z = z;
        return data;
    }));

    private File dir = null;
    private final String fileName = "wormhole_data.json";

    int x = 0;
    int z = 0;

    public void init(MinecraftServer server) {
        dir = DimensionUtils.getDimensionDir(server);
        load();
        save();
    }

    public void load() {
        if(dir == null) return;

        JsonElement json = JsonUtils.loadFromJson(dir, fileName, JsonParser.parseString("{}"));
        DataResult<Pair<UltraSpaceData, JsonElement>> result = CODEC.decode(JsonOps.INSTANCE, json);

        if(result.isSuccess()) {
            UltraSpaceData newData = result.getOrThrow().getFirst();

            x = newData.x;
            z = newData.z;
        } else {
            save();
        }
    }

    public void save() {
        if(dir == null) return;
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        JsonUtils.saveToJson(dir, fileName, result.getOrThrow());
    }

    public GridPos getCurrentGridCoords() { return new GridPos(x, z); }

    public GridPos getNextGridCoords() {
        if(dir == null) return null;

        GridPos pos = new GridPos(x, z);

        if(x > z) {
            z++;
            if(x == z) x = 0;
        } else {
            if(x == z) z = 0;
            x++;
        }

        save();

        return pos;
    }

}
