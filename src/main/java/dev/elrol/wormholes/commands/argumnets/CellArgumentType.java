package dev.elrol.wormholes.commands.argumnets;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.registries.CellRegistry;

import java.util.concurrent.CompletableFuture;

public class CellArgumentType implements ArgumentType<CellData> {

    public static CellArgumentType cell() { return new CellArgumentType(); }

    @Override
    public CellData parse(StringReader reader) throws CommandSyntaxException {
        String cellID = reader.readString();
        return CellRegistry.getCell(cellID);
    }

    @Override
    public <S> CellData parse(StringReader reader, S source) throws CommandSyntaxException {
        return parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CellRegistry.CELL_DATA_MAP.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static <T> CellData getCell(String name, CommandContext<T> context) {
        return context.getArgument(name, CellData.class);
    }
}
