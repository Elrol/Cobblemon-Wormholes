package dev.elrol.wormholes;

import dev.elrol.wormholes.commands.WormholeCommand;
import dev.elrol.wormholes.commands.argumnets.CellArgumentType;
import dev.elrol.wormholes.data.UltraSpaceData;
import dev.elrol.wormholes.entities.WormholeEntity;
import dev.elrol.wormholes.libs.WormholeConstants;
import dev.elrol.wormholes.registries.CellRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Wormholes implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(WormholeConstants.MODID);
    public static WormholeConfig CONFIG = WormholeConfig.load();
    public static UltraSpaceData ultraSpaceData = new UltraSpaceData();

    public static final EntityType<WormholeEntity> WORMHOLE_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(WormholeConstants.MODID, "wormhole"),
            EntityType.Builder.create(WormholeEntity::new, SpawnGroup.MISC).dimensions(10.0f, 10.0f).build("wormhole")
    );

    private static int SPAWN_INTERVAL = 0;

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(WormholeConstants.MODID, "cell_argument"), // The unique ID for your argument type
                CellArgumentType.class, // Your custom ArgumentType class
                ConstantArgumentSerializer.of(CellArgumentType::new) // The serializer for your argument
        );

        registerEvents();

        CellRegistry.load();
        CONFIG.save();
    }

    private void registerEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> WormholeCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ultraSpaceData.init(server));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ultraSpaceData.save());

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            CONFIG = WormholeConfig.load();
            CellRegistry.load();
            CellRegistry.update();
            ultraSpaceData.load();
            debug("Wormholes Data Reloaded");
        });

        ServerTickEvents.END_SERVER_TICK.register(Wormholes::scheduleWormholeSpawn);
    }

    private static void scheduleWormholeSpawn(MinecraftServer server) {
        WormholeConfig.EntityConfig entityConfig = CONFIG.entity;
        Random rand = new Random();
        if(SPAWN_INTERVAL <= 0) {
            SPAWN_INTERVAL = rand.nextInt(entityConfig.getMinSpawnInterval(), entityConfig.getMaxSpawnInterval()) * 1200;
            debug("Spawn interval being set to {}", SPAWN_INTERVAL);

            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                float numPicked = rand.nextFloat(0.0f, 1.0f);
                if(player.getWorld().equals(server.getOverworld()) && numPicked <= entityConfig.getWormholeSpawnChance()) {
                    trySpawnWormhole(player);
                } else {
                    debug("Is in world: {}", player.getWorld().getRegistryKey().getValue());
                    debug("Number Picked: {}", numPicked);
                }
            }
        } else {
            SPAWN_INTERVAL--;
        }
    }

    public static void trySpawnWormhole(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        WormholeConfig.EntityConfig entityConfig = CONFIG.entity;
        Random rand = new Random();

        int spawnDistance = entityConfig.getMinSpawnRadius() + rand.nextInt(entityConfig.getMaxSpawnRadius() - entityConfig.getMinSpawnRadius());
        double angle = rand.nextDouble() * 2 * Math.PI;

        int wormholeSize = Math.round(entityConfig.getWormholeScale() * 5);
        int posOffset = Math.round((wormholeSize - 1.0f) / 2.0f);

        int x = (int) (player.getX() + spawnDistance * Math.cos(angle));
        int z = (int) (player.getZ() + spawnDistance * Math.sin(angle));

        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) + entityConfig.getSpawnHeight() + posOffset;

        BlockPos spawnPos = new BlockPos(x, groundY, z);

        boolean isEmpty = true;

        BlockPos origin = spawnPos.subtract(new BlockPos(posOffset, posOffset, posOffset));

        debug("Attempting to spawn a wormhole: {} : {}", spawnPos, world.getBlockState(spawnPos));
        debug("Offset Origin: {} : posOffset: {}", origin, posOffset);

        for(int x1 = 0; x1 < wormholeSize; x1++) {
            for(int y1 = 0; y1 < wormholeSize; y1++) {
                for(int z1 = 0; z1 < wormholeSize; z1++) {
                    BlockState state = world.getBlockState(origin.add(new BlockPos(x1, y1, z1)));
                    if(!state.isAir()) {
                        isEmpty = false;
                        debug("Not air: {}", state);
                        break;
                    }
                }
            }
        }

        if(isEmpty) spawnWormhole(world, spawnPos);
    }

    private static void spawnWormhole(ServerWorld world, BlockPos pos) {
        WormholeEntity entity = new WormholeEntity(WORMHOLE_ENTITY_TYPE, world);
        entity.setPosition(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        world.spawnEntity(entity);
        debug("Spawned wormhole: {}", pos);
    }

    public static void debug(String message) {
        if(CONFIG == null || CONFIG.isDebug) Wormholes.LOGGER.warn(message);
    }
    public static void debug(String format, Object arg) {
        if(CONFIG == null || CONFIG.isDebug) Wormholes.LOGGER.warn(format, arg);
    }

    public static void debug(String format, Object... arguments) {
        if(CONFIG == null || CONFIG.isDebug) Wormholes.LOGGER.warn(format, arguments);
    }

}
