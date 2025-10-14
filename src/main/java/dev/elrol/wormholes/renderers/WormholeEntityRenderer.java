package dev.elrol.wormholes.renderers;

import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.entities.WormholeEntity;
import dev.elrol.wormholes.libs.WormholeConstants;
import dev.elrol.wormholes.models.WormholeEntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public class WormholeEntityRenderer extends GeoEntityRenderer<WormholeEntity> {

    public static final Map<WormholeEntity.WormholeVariant, Identifier> TEXTURES = new HashMap<>();

    static {
        TEXTURES.put(WormholeEntity.WormholeVariant.default_variant, Identifier.of(WormholeConstants.MODID, "textures/entity/wormhole_default.png"));
        TEXTURES.put(WormholeEntity.WormholeVariant.in, Identifier.of(WormholeConstants.MODID, "textures/entity/wormhole_in.png"));
        TEXTURES.put(WormholeEntity.WormholeVariant.out, Identifier.of(WormholeConstants.MODID, "textures/entity/wormhole_out.png"));
    }

    public WormholeEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new WormholeEntityModel());
    }

    @Override
    public void preRender(MatrixStack poseStack, WormholeEntity animatable, BakedGeoModel model, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        float scale = Wormholes.CONFIG.entity.wormholeScale;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTexture(WormholeEntity animatable) {
        return TEXTURES.get(animatable.getVariant());
    }
}
