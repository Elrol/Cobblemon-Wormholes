package dev.elrol.wormholes.libs;

import com.google.gson.Gson;
import dev.elrol.wormholes.Wormholes;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {

    public static void saveToJson(File dir, String name, Object obj) {
        Gson GSON = WormholeConstants.makeGSON();
        File file = new File(dir, name);

        if(dir.mkdirs()) {
            Wormholes.debug("Directory created: {}", dir);
        }

        try(FileWriter writer = new FileWriter(file)) {
            GSON.toJson(obj, writer);
            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
                Wormholes.debug("Saved File " + name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T loadFromJson(File dir, String name, T defaultObject) {
        File file = new File(dir, name);

        if(file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson GSON = WormholeConstants.makeGSON();
                T obj = GSON.fromJson(reader, (Class<T>) defaultObject.getClass());

                if (obj != null) {
                    Wormholes.debug("Loaded File " + name);
                    return obj;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            saveToJson(dir, name, defaultObject);
        }

        return defaultObject;

    }
}
