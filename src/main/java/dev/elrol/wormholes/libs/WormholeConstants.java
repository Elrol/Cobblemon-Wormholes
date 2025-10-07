package dev.elrol.wormholes.libs;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.elrol.wormholes.data.adapter.RegistryKeyWorldTypeAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.File;

public class WormholeConstants {

    public static final String MODID = "cobblemon_wormholes";

    public static final File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(),"/Wormholes");
    public static final Identifier ULTRA_SPACE_DIM = Identifier.of("cobblemon_wormholes", "ultra_space");
    public static final RegistryKey<World> ULTRA_SPACE_KEY = RegistryKey.of(RegistryKeys.WORLD, WormholeConstants.ULTRA_SPACE_DIM);

    public static Gson makeGSON() {
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(new TypeToken<RegistryKey<World>>(){}.getType(), new RegistryKeyWorldTypeAdapter()).create();
    }

}
