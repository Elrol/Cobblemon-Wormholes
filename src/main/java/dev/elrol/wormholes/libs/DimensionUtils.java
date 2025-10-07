package dev.elrol.wormholes.libs;

import dev.elrol.wormholes.WormholeConfig;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.data.GridPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class DimensionUtils {

    public static ServerWorld getUltraSpace(MinecraftServer server) {
        return server.getWorld(WormholeConstants.ULTRA_SPACE_KEY);
    }

    public static File getDimensionDir(MinecraftServer server) {
        RegistryKey<World> key = WormholeConstants.ULTRA_SPACE_KEY;
        return DimensionType.getSaveDirectory(key, server.getSavePath(WorldSavePath.ROOT)).toFile();
    }

    public static void tryPlaceCell(MinecraftServer server, CellData cell) {
        WormholeConfig config = Wormholes.CONFIG;
        ServerWorld ultraSpaceWorld = DimensionUtils.getUltraSpace(server);
        ultraSpaceWorld.getChunk(cell.getCellOffset());

        GridPos gridPos = Wormholes.ultraSpaceData.getNextGridCoords();
        if(gridPos == null) {
            Wormholes.LOGGER.error("Grid pos was null when trying to place cell");
            return;
        }

        int cellSize = config.dimensions.getCellSize();
        int blocksBetween = config.dimensions.getChunksBetweenCells() * 16;

        BlockPos targetLocation = new BlockPos(
                (cellSize * gridPos.getX()) + (blocksBetween * (gridPos.getX() - 1)),
                config.dimensions.getCellElevation(),
                (cellSize * gridPos.getY()) + (blocksBetween * (gridPos.getY() - 1))
        );

        targetLocation.add(cell.getCellOffset());


        ChunkPos chunkPos = new ChunkPos(targetLocation);
        CompletableFuture<?> future = ultraSpaceWorld.getChunkManager().getChunkFutureSyncOnMainThread(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

        future.thenRun(() -> {
            String command = "execute in " + WormholeConstants.ULTRA_SPACE_DIM + " run place template " + cell.getSchematic() + " " + targetLocation.getX() + " " + targetLocation.getY() + " " + targetLocation.getZ();
            ServerCommandSource source = server.getCommandSource();
            server.getCommandManager().executeWithPrefix(source, command);
            Wormholes.debug("Generating Structure at: {}", targetLocation.toShortString());
        });
    }

}
