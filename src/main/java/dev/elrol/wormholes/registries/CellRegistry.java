package dev.elrol.wormholes.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.libs.JsonUtils;
import dev.elrol.wormholes.libs.WormholeConstants;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CellRegistry {

    private static final File dir = new File(WormholeConstants.configDir, "/Cells");
    private static final File schematicDir = new File(WormholeConstants.configDir, "/Schematics");

    public static final Map<String, CellData> CELL_DATA_MAP = new HashMap<>();

    public static void registerCell(CellData cellData) {
        if(cellData != null) {
            CELL_DATA_MAP.put(cellData.getCellID(), cellData);
        }
    }

    public static CellData getCell() {
        List<String> keys = CELL_DATA_MAP.keySet().stream().toList();
        String key = keys.get(new Random().nextInt(0, keys.size()));
        return getCell(key);
    }

    @Nullable
    public static CellData getCell(String id) {
        return CELL_DATA_MAP.get(id);
    }

    public static void load() {
        File[] files = dir.listFiles(s -> s.getName().endsWith(".json"));
        if(schematicDir.mkdir()) {
            Wormholes.debug("Schematic Directory Created: {}", schematicDir);
        }

        boolean shouldSave = false;

        if(files != null) {
            for (File file : files) {
                String fileName = file.getName();
                JsonElement json = JsonUtils.loadFromJson(dir, fileName, JsonParser.parseString("{}"));
                DataResult<Pair<CellData, JsonElement>> result = CellData.CODEC.decode(JsonOps.INSTANCE, json);
                CellData loadedCell = result.getOrThrow().getFirst();
                registerCell(loadedCell);
            }
        }

        if(CELL_DATA_MAP.isEmpty()) {
            CellData cellData = new CellData("example_1", new File(schematicDir, "example_1.schem"));
            registerCell(cellData);
            shouldSave = true;
        }

        if(shouldSave) save();
    }

    public static void save() {
        CELL_DATA_MAP.forEach((id, cell) -> {
            DataResult<JsonElement> result = CellData.CODEC.encodeStart(JsonOps.INSTANCE, cell);
            JsonUtils.saveToJson(dir, id + ".json", result.getOrThrow());
        });
    }

}
