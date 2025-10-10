package dev.elrol.wormholes.models;

import dev.elrol.wormholes.entities.WormholeEntity;
import dev.elrol.wormholes.libs.WormholeConstants;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WormholeEntityModel extends GeoModel<WormholeEntity> {

    private static final Identifier MODEL       = Identifier.of(WormholeConstants.MODID, "geo/wormhole.geo.json");
    private static final Identifier TEXTURE     = Identifier.of(WormholeConstants.MODID, "textures/entity/wormhole.png");
    private static final Identifier ANIMATION   = Identifier.of(WormholeConstants.MODID, "animations/wormhole.animation.json");

    @Override
    public Identifier getModelResource(WormholeEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(WormholeEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(WormholeEntity animatable) {
        return ANIMATION;
    }
}
