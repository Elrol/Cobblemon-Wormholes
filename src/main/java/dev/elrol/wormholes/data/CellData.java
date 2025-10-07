package dev.elrol.wormholes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;


public class CellData {

    public static final Codec<CellData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("cellOffset").forGetter(CellData::getCellOffset),
        Identifier.CODEC.fieldOf("schematic").forGetter(CellData::getSchematic)
    ).apply(instance, (cellOffset, schematic) -> {
        CellData data = new CellData();
        data.cellOffset = cellOffset;
        data.schematic = schematic;
        return data;
    }));

    public static final CellData EMPTY = new CellData();

    BlockPos cellOffset = new BlockPos(0,0,0);
    Identifier schematic;

    public CellData() {
        this.schematic = null;
    }

    public CellData(Identifier schematic) {
        this.schematic = schematic;
    }

    public CellData(Identifier schematic, BlockPos cellOffset) {
        this.schematic = schematic;
        this.cellOffset = cellOffset;
    }

    public boolean isEmpty() {
        return equals(EMPTY);
    }

    public Identifier getSchematic() { return schematic; }

    public BlockPos getCellOffset() { return cellOffset; }
}
