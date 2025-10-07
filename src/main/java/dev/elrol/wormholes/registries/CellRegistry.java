package dev.elrol.wormholes.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.libs.JsonUtils;
import dev.elrol.wormholes.libs.WormholeConstants;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CellRegistry {

    private static final File dir = new File(WormholeConstants.configDir, "/Cells");

    public static final Map<String, CellData> CELL_DATA_MAP = new HashMap<>();

    public static void registerCell(String id, CellData cellData) {
        CELL_DATA_MAP.put(id, cellData);
    }

    public static CellData getCell(String id) {
        return CELL_DATA_MAP.getOrDefault(id, new CellData());
    }

    public static void load() {
        File[] files = dir.listFiles(s -> s.getName().endsWith(".json"));

        if(files != null) {
            for (File file : files) {
                String pathName = file.getName();
                JsonElement json = JsonUtils.loadFromJson(dir, pathName, JsonParser.parseString("{}"));
                DataResult<Pair<CellData, JsonElement>> result = CellData.CODEC.decode(JsonOps.INSTANCE, json);
                CellData loadedCell = result.getOrThrow().getFirst();
                registerCell(pathName.replace(".json", ""), loadedCell);
            }
        }

        if(CELL_DATA_MAP.isEmpty()) {
            registerCell("example_1", new CellData(Identifier.of("cobblemon_wormholes", "example_1")));
            save();
        }
    }

    public static void save() {
        CELL_DATA_MAP.forEach((id, cell) -> {
            DataResult<JsonElement> result = CellData.CODEC.encodeStart(JsonOps.INSTANCE, cell);
            JsonUtils.saveToJson(dir, id + ".json", result.getOrThrow());
        });
    }

}
