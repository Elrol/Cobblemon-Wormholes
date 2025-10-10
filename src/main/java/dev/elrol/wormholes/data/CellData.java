package dev.elrol.wormholes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.libs.DimensionUtils;
import net.minecraft.util.math.BlockPos;

import java.io.File;


public class CellData {

    public static final Codec<CellData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("cellID").forGetter(CellData::getCellID),
        BlockPos.CODEC.fieldOf("cellOffset").forGetter(CellData::getCellOffset),
        BlockPos.CODEC.fieldOf("spawnOffset").forGetter(CellData::getSpawnOffset),
        Codec.STRING.fieldOf("schematic").forGetter(data -> data.getSchematic().toString())
    ).apply(instance, (cellID, cellOffset, spawnOffset, schematic) -> {
        CellData data = new CellData(cellID, new File(schematic));
        data.cellOffset = cellOffset;
        data.spawnOffset = spawnOffset;
        return data;
    }));

    final String cellID;
    BlockPos cellOffset = new BlockPos(0,0,0);
    BlockPos spawnOffset = new BlockPos(0,0,0);
    File schematic;

    public CellData(String id, File schematic) {
        this.cellID = id;
        this.schematic = schematic;
    }

    public boolean update() {
        boolean updated = false;

        if (schematic.exists()) {
            if(cellOffset.equals(new BlockPos(0,0,0))) {
                Wormholes.debug("Updating Schematic Position");
                Clipboard clipboard = DimensionUtils.loadSchematic(schematic);
                if (clipboard != null) {
                    BlockVector3 b3 = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
                    cellOffset = new BlockPos(-b3.x(), -b3.y(), -b3.z());
                    updated = true;
                }
            }

            if(spawnOffset.equals(new BlockPos(0,0,0))) {
                Wormholes.debug("Updating Spawn Position");
                    spawnOffset = cellOffset.multiply(-1);
                    updated = true;
            }
        }
        return updated;
    }

    public String getCellID() { return cellID; }
    public BlockPos getCellOffset() { return cellOffset; }
    public BlockPos getSpawnOffset() { return spawnOffset; }
    public File getSchematic() { return schematic; }
}
