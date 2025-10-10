package dev.elrol.wormholes;

import dev.elrol.wormholes.renderers.WormholeEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class WormholeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Wormholes.WORMHOLE_ENTITY_TYPE, WormholeEntityRenderer::new);
    }
}
