package dev.elrol.wormholes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GridPos {

    public static final Codec<GridPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("x").forGetter(GridPos::getX),
        Codec.INT.fieldOf("y").forGetter(GridPos::getY)
    ).apply(instance, GridPos::new));

    int x;
    int y;

    public GridPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public GridPos copy() {
        return new GridPos(x, y);
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public void subtract(int x, int y) {
        add(-x, -y);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GridPos gridPos) {
            return x == gridPos.getX() && y == gridPos.getY();
        }
        return false;
    }
}
