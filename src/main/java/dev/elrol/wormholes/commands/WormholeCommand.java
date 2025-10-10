package dev.elrol.wormholes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.elrol.wormholes.Wormholes;
import dev.elrol.wormholes.commands.argumnets.CellArgumentType;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.data.PlacedCellData;
import dev.elrol.wormholes.libs.DimensionUtils;
import dev.elrol.wormholes.registries.CellRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class WormholeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("wormhole")
                .then(literal("generate")
                        .executes(WormholeCommand::generateRandom)
                        .then(argument("cell", CellArgumentType.cell())
                            .executes(WormholeCommand::generate)))
                .then(literal("teleport")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(WormholeCommand::teleport))));
    }

    private static int generateRandom(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int i = DimensionUtils.tryPlaceCell(source.getServer());
        if(i >= 0) {
            source.sendMessage(Text.literal("Generated the new Cell with an index of " + i));
            return 1;
        }
        source.sendMessage(Text.literal("Cell generation failed"));
        return 0;
    }

    private static int generate(CommandContext<ServerCommandSource> context) {
        CellData cell = CellArgumentType.getCell("cell", context);

        if(cell != null) {
            ServerCommandSource source = context.getSource();
            int i = DimensionUtils.tryPlaceCell(source.getServer(), cell);
            source.sendMessage(Text.literal("Generated the new Cell with an index of " + i));
        }

        return 1;
    }

    private static int teleport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        int index = IntegerArgumentType.getInteger(context, "index");

        List<PlacedCellData> placedCells = Wormholes.ultraSpaceData.placedCellDataList;
        if(index < placedCells.size()) {
            PlacedCellData placedCell = placedCells.get(index);
            placedCell.teleport(player);
            player.sendMessage(Text.literal("Teleporting through wormhole"));
            return 1;
        }

        return 0;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return CommandManager.literal(name);
    }

    private static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return CommandManager.argument(name, type);
    }

}
