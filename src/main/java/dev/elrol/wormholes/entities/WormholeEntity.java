package dev.elrol.wormholes.entities;

import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.data.GridPos;
import dev.elrol.wormholes.data.PlacedCellData;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.registries.CellRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class WormholeEntity extends Entity implements GeoEntity {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.wormhole.idle");
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(WormholeEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public enum WormholeVariant {
        default_variant("default"),
        in("in"),
        out("out");


        private final String name;

        WormholeVariant(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int wormholeIndex = -2;
    private BlockPos teleportLocation = null;

    public WormholeEntity(EntityType<? extends WormholeEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        World world = getWorld();
        if(!world.isClient()) {
            Box boundingBox = getBoundingBox();
            List<Entity> touchingEntities = world.getOtherEntities(this, boundingBox, Entity::isPlayer);

            for (Entity touchingEntity : touchingEntities) {
                if(touchingEntity instanceof ServerPlayerEntity player) {
                    MinecraftServer server = player.getServer();
                    if(server != null) {
                        if (player.getWorld().equals(server.getOverworld())) {
                            if (wormholeIndex == -2) {
                                wormholeIndex = DimensionUtils.tryPlaceCell(server);
                            }

                            if (wormholeIndex >= 0) {
                                PlacedCellData placedData = Wormholes.ultraSpaceData.getPlacedCell(wormholeIndex);
                                if (placedData != null) {
                                    if(teleportLocation == null) {
                                        CellData cell = CellRegistry.getCell(placedData.getCellID());
                                        GridPos gridPos = Wormholes.ultraSpaceData.calcGridPos(placedData.getCellIndex());

                                        if(cell == null) return;
                                        BlockPos offset = new BlockPos(0, Wormholes.CONFIG.entity.getSpawnHeight(), 0);

                                        // Get the location to place the player at in Ultra Space
                                        linkPortal(DimensionUtils.getCellPlayerSpawn(cell, gridPos));

                                        ServerWorld ultraSpace = DimensionUtils.getUltraSpace(server);
                                        WormholeEntity otherWormhole = new WormholeEntity(Wormholes.WORMHOLE_ENTITY_TYPE, ultraSpace);

                                        BlockPos pos = teleportLocation.add(offset);
                                        otherWormhole.setPos(pos.getX(), pos.getY(), pos.getZ());

                                        otherWormhole.linkPortal(getBlockPos().subtract(offset));
                                        ultraSpace.spawnEntity(otherWormhole);
                                    }
                                    teleport(player);
                                    //placedData.teleport(player);
                                }
                            }
                        } else {
                            teleport(player);
                        }
                    }
                }
            }
        }
    }

    private void linkPortal(BlockPos target) {
        MinecraftServer server = getServer();
        if(server == null) return;
        setVariant(getWorld().equals(server.getOverworld()) ? WormholeVariant.in : WormholeVariant.out);
        teleportLocation = target;
    }

    private void teleport(ServerPlayerEntity player) {
        MinecraftServer server = getServer();

        if(server == null || teleportLocation == null) return;

        ServerWorld world = getWorld().equals(server.getOverworld()) ? DimensionUtils.getUltraSpace(server) : server.getOverworld();

        player.teleport(world,
                teleportLocation.getX() + 0.5f,
                teleportLocation.getY() + 0.5f,
                teleportLocation.getZ() + 0.5f,
                0.0f,
                0.0f);
    }

    public WormholeVariant getVariant() { return WormholeVariant.values() [dataTracker.get(VARIANT)]; }
    public void setVariant(WormholeVariant newVariant) { dataTracker.set(VARIANT, newVariant.ordinal()); }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(VARIANT, 0);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

        if(nbt.contains("teleportLocation")) {
            int[] array = nbt.getIntArray("teleportLocation");
            teleportLocation = new BlockPos(array[0], array[1], array[2]);
        }

        if(nbt.contains("variant"))
            dataTracker.set(VARIANT, nbt.getInt("variant"));

        if(nbt.contains("wormholeIndex"))
            wormholeIndex = nbt.getInt("wormholeIndex");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if(teleportLocation != null)
            nbt.putIntArray("teleportLocation", new int[]{
                    teleportLocation.getX(),
                    teleportLocation.getY(),
                    teleportLocation.getZ()
            });
        nbt.putInt("variant", dataTracker.get(VARIANT));
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

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if(getWorld().isClient()) {
            if(VARIANT.equals(data)) {
                calculateDimensions();
            }
        }
    }
}
