package dev.elrol.wormholes.data.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;

public class RegistryKeyWorldTypeAdapter extends TypeAdapter<RegistryKey<World>> {
    @Override
    public void write(JsonWriter out, RegistryKey<World> value) throws IOException {
        Identifier v = value.getValue();
        out.value(v.getNamespace() + ":" + v.getPath());
    }

    @Override
    public RegistryKey<World> read(JsonReader in) throws IOException {
        String[] i = in.nextString().split(":");
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(i[0], i[1]));
    }
}
