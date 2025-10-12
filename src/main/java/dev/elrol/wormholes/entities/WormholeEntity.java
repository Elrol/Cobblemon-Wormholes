package dev.elrol.wormholes.entities;

import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.data.PlacedCellData;
import dev.elrol.wormholes.libs.DimensionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class WormholeEntity extends Entity implements GeoEntity {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.wormhole.idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private boolean spawned = false;
    private int wormholeIndex = -2;

    public WormholeEntity(EntityType<? extends WormholeEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.getWorld().isClient) {
            Box boundingBox = getBoundingBox();
            List<Entity> touchingEntities = getWorld().getOtherEntities(this, boundingBox, (entity) -> entity instanceof ServerPlayerEntity);

            for (Entity touchingEntity : touchingEntities) {
                if(touchingEntity instanceof ServerPlayerEntity player) {
                    if(wormholeIndex == -2) {
                        MinecraftServer server = player.getServer();
                        wormholeIndex = DimensionUtils.tryPlaceCell(server);
                    }

                    if(wormholeIndex >= 0) {
                        PlacedCellData placedData = Wormholes.ultraSpaceData.getPlacedCell(wormholeIndex);
                        if(placedData != null) {
                            placedData.teleport(player);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if(nbt.contains("spawned"))
            spawned = nbt.getBoolean("spawned");

        if(nbt.contains("wormholeIndex"))
            wormholeIndex = nbt.getInt("wormholeIndex");

        if(!getWorld().isClient && !spawned) {
            DimensionUtils.updateLight(getWorld(), getBlockPos().down(2));
            spawned = true;
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("spawned", spawned);
        nbt.putInt("wormholeIndex", wormholeIndex);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Idling", 5, this::idleAnimController));
    }

    protected <T extends WormholeEntity>PlayState idleAnimController(final AnimationState<T> event) {
        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void onRemoved() {
        DimensionUtils.removeLight(getWorld(), getBlockPos().down(2));
        super.onRemoved();
    }
}
