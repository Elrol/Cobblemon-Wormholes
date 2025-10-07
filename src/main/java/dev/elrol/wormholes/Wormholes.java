package dev.elrol.wormholes;

import dev.elrol.wormholes.commands.WormholeCommand;
import dev.elrol.wormholes.commands.argumnets.CellArgumentType;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.data.GridPos;
import dev.elrol.wormholes.data.UltraSpaceData;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.libs.WormholeConstants;
import dev.elrol.wormholes.registries.CellRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wormholes implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(WormholeConstants.MODID);
    public static WormholeConfig CONFIG = WormholeConfig.load();
    public static UltraSpaceData ultraSpaceData = new UltraSpaceData();

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

            CellData cell = CellRegistry.getCell("example_1");
            if(!cell.equals(CellData.EMPTY) && ultraSpaceData.getCurrentGridCoords().equals(new GridPos(0,0))) {
                debug("Trying to place example cell");
                DimensionUtils.tryPlaceCell(server, cell);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {});

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            CONFIG = WormholeConfig.load();
            CellRegistry.load();
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
