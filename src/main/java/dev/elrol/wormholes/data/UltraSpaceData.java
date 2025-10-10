package dev.elrol.wormholes.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.wormholes.events.CellPlacedCallback;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.libs.JsonUtils;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UltraSpaceData {

    public static final Codec<UltraSpaceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlacedCellData.CODEC.listOf().fieldOf("placedCellDataList").forGetter(data -> data.placedCellDataList)
    ).apply(instance, (placedCellDataList) -> {
        UltraSpaceData data = new UltraSpaceData();
        data.placedCellDataList.addAll(placedCellDataList);
        return data;
    }));

    private File dir = null;
    private final String fileName = "wormhole_data.json";

    public List<PlacedCellData> placedCellDataList = new ArrayList<>();

    public void init(MinecraftServer server) {
        dir = DimensionUtils.getDimensionDir(server);
        load();
        save();
    }

    public GridPos calcGridPos(int index) {
        if(index < 0) return null;

        int m = (int) Math.sqrt(index);
        int k = index - (m * m);

        int x, z;

        if(k < m) {
            x = m;
            z = k;
        } else {
            x = k-m;
            z = m;
        }
        return new GridPos(x, z);
    }

    public GridPos calcNextGridPos() {
        return calcGridPos(placedCellDataList.size());
    }

    public void load() {
        if(dir == null) return;

        JsonElement json = JsonUtils.loadFromJson(dir, fileName, JsonParser.parseString("{}"));
        DataResult<Pair<UltraSpaceData, JsonElement>> result = CODEC.decode(JsonOps.INSTANCE, json);

        if(result.isSuccess()) {
            UltraSpaceData newData = result.getOrThrow().getFirst();
            placedCellDataList.clear();
            placedCellDataList.addAll(newData.placedCellDataList);
        } else {
            save();
        }
    }

    public void save() {
        if(dir == null) return;
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        JsonUtils.saveToJson(dir, fileName, result.getOrThrow());
    }

    public GridPos getCurrentGridCoords() {
        if(placedCellDataList.isEmpty()) return null;
        return calcGridPos(placedCellDataList.size() - 1);
    }

    public void placedCell(String cellID, int cellIndex) {
        PlacedCellData placedCell = new PlacedCellData(cellID, cellIndex);
        placedCellDataList.add(placedCell);
        CellPlacedCallback.EVENT.invoker().placed(placedCell);
        save();
    }

    @Nullable
    public PlacedCellData getPlacedCell(int cellIndex) {
        if(placedCellDataList.isEmpty() || cellIndex >= placedCellDataList.size()) return null;
        return placedCellDataList.get(cellIndex);
    }

}
