package dev.elrol.wormholes.renderers;

import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.entities.WormholeEntity;
import dev.elrol.wormholes.models.WormholeEntityModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WormholeEntityRenderer extends GeoEntityRenderer<WormholeEntity> {

    public WormholeEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new WormholeEntityModel());
    }

    @Override
    public void preRender(MatrixStack poseStack, WormholeEntity animatable, BakedGeoModel model, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        float scale = Wormholes.CONFIG.wormholeScale;
        poseStack.scale(scale, scale, scale);
    }
}
