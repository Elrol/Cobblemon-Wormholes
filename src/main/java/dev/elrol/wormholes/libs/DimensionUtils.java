package dev.elrol.wormholes.libs;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import dev.elrol.wormholes.WormholeConfig;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.data.GridPos;
import dev.elrol.wormholes.data.UltraSpaceData;
import dev.elrol.wormholes.registries.CellRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class DimensionUtils {

    private static final BlockState LIGHT_BLOCK = Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15);

    public static ServerWorld getUltraSpace(MinecraftServer server) {
        return server.getWorld(WormholeConstants.ULTRA_SPACE_KEY);
    }

    public static File getDimensionDir(MinecraftServer server) {
        RegistryKey<World> key = WormholeConstants.ULTRA_SPACE_KEY;
        return DimensionType.getSaveDirectory(key, server.getSavePath(WorldSavePath.ROOT)).toFile();
    }

    public static int tryPlaceCell(MinecraftServer server) {
        CellData cell = CellRegistry.getCell();
        if(cell != null) return tryPlaceCell(server, cell);
        return -1;
    }

    public static int tryPlaceCell(MinecraftServer server, String cellID) {
        CellData cell = CellRegistry.getCell(cellID);
        if(cell == null) return -1;
        return tryPlaceCell(server, cell);
    }

    public static int tryPlaceCell(MinecraftServer server, CellData cell) {
        ServerWorld ultraSpaceWorld = DimensionUtils.getUltraSpace(server);
        ultraSpaceWorld.getChunk(cell.getCellOffset());

        UltraSpaceData spaceData = Wormholes.ultraSpaceData;
        int nextCellIndex = spaceData.placedCellDataList.size();
        GridPos gridPos = spaceData.calcGridPos(nextCellIndex);

        if(gridPos == null) {
            Wormholes.LOGGER.error("Grid pos was null when trying to place cell");
            return -1;
        }

        if(cell.update()) {
            CellRegistry.registerCell(cell);
            CellRegistry.save();
        }

        BlockPos targetLocation = getCellOrigin(gridPos);
        targetLocation.add(cell.getCellOffset());

        if(placeSchematic(ultraSpaceWorld, cell.getSchematic(), targetLocation)) {
            Wormholes.debug("Generating Structure at: {}", targetLocation.toShortString());
            Wormholes.ultraSpaceData.placedCell(cell.getCellID(), nextCellIndex);
            return nextCellIndex;
        } else {
            Wormholes.LOGGER.error("Schematic placed failed");
            return -1;
        }
    }

    public static BlockPos getCellOrigin(GridPos gridPos) {
        WormholeConfig config = Wormholes.CONFIG;
        int cellSize = config.dimensions.getCellSize();
        int blocksBetween = config.dimensions.getChunksBetweenCells() * 16;

        return new BlockPos(
                (cellSize * gridPos.getX()) + (blocksBetween * gridPos.getX()),
                config.dimensions.getCellElevation(),
                (cellSize * gridPos.getY()) + (blocksBetween * gridPos.getY())
        );
    }

    public static BlockPos getCellPlayerSpawn(CellData cell, GridPos gridPos) {
        BlockPos origin = getCellOrigin(gridPos);
        return origin.add(cell.getSpawnOffset());
    }

    public static boolean placeSchematic(ServerWorld world, File file, BlockPos origin) {
        Clipboard clipboard = loadSchematic(file);

        if(clipboard == null) return false;

        try(EditSession editSession = WorldEdit.getInstance().newEditSession(FabricAdapter.adapt(world))) {
            editSession.setSideEffectApplier(SideEffectSet.none());
            editSession.setTrackingHistory(false);

            BlockVector3 max = clipboard.getMaximumPoint();
            BlockVector3 min = clipboard.getMinimumPoint();
            BlockVector3 playerOrigin = clipboard.getOrigin();

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(origin.getX(), origin.getY(), origin.getZ()))
                    .copyBiomes(true)
                    .copyEntities(true)
                    .ignoreAirBlocks(true)
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @SuppressWarnings("all")
    public static Clipboard loadSchematic(File file) {
        Clipboard clipboard = null;

        if(file.exists()) {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format != null) {
                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    clipboard = reader.read();
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return clipboard;
    }

    public static void updateLight(World world, BlockPos pos) {
        if(world.getBlockState(pos).isReplaceable()) {
            world.setBlockState(pos, LIGHT_BLOCK, 2);
            Wormholes.debug("Placed light at {}", pos);
        }
    }

    public static void removeLight(World world, BlockPos pos) {
        if(world.getBlockState(pos).isOf(Blocks.LIGHT)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            Wormholes.debug("Removed light from {}", pos);
        }
    }

}
