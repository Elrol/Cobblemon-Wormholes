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
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wormholes implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(WormholeConstants.MODID);
    public static WormholeConfig CONFIG = WormholeConfig.load();
    public static UltraSpaceData ultraSpaceData = new UltraSpaceData();

    public static final EntityType<WormholeEntity> WORMHOLE_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(WormholeConstants.MODID, "wormhole"),
            EntityType.Builder.create(WormholeEntity::new, SpawnGroup.MISC).dimensions(10.0f, 10.0f).build("wormhole")
    );

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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            WormholeCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ultraSpaceData.init(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ultraSpaceData.save();
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            CONFIG = WormholeConfig.load();
            CellRegistry.load();
            CellRegistry.update();
            ultraSpaceData.load();
            debug("Wormholes Data Reloaded");
        });
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
