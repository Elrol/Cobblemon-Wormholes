package dev.elrol.wormholes;

import dev.elrol.wormholes.libs.WormholeConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wormholes implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(WormholeConstants.MODID);
    public static WormholeConfig CONFIG = WormholeConfig.load();

    @Override
    public void onInitialize() {
        if(FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) return;

        registerEvents();
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {});
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {});
    }

    public static void debug(String message) {
        if(CONFIG == null || CONFIG.isDebug) Wormholes.LOGGER.warn(message);
    }

}
