package dev.elrol.wormholes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.registries.CellRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class PlacedCellData {

    public static final Codec<PlacedCellData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("cellID").forGetter(data -> data.cellID),
        Codec.INT.fieldOf("cellIndex").forGetter(data -> data.cellIndex)
    ).apply(instance, PlacedCellData::new));

    final String cellID;
    final int cellIndex;

    public PlacedCellData(String cellID, int cellIndex) {
        this.cellID = cellID;
        this.cellIndex = cellIndex;
    }

    public void teleport(ServerPlayerEntity player) {
        CellData cell = CellRegistry.getCell(cellID);
        if(cell == null) return;

        GridPos gridPos = Wormholes.ultraSpaceData.calcGridPos(cellIndex);
        BlockPos position = DimensionUtils.getCellPlayerSpawn(cell, gridPos);

        MinecraftServer server = player.getServer();
        if(server != null) {
            player.teleport(DimensionUtils.getUltraSpace(server), position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, 0, 0);
        }
    }

    public String getCellID() {
        return cellID;
    }

    public int getCellIndex() {
        return cellIndex;
    }
}
