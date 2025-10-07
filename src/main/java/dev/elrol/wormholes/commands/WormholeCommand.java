package dev.elrol.wormholes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.elrol.wormholes.commands.argumnets.CellArgumentType;
import dev.elrol.wormholes.data.CellData;
import dev.elrol.wormholes.libs.DimensionUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class WormholeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("wormhole")
                .then(CommandManager.literal("generate")
                        .then(CommandManager.argument("cell", CellArgumentType.cell())
                            .executes(WormholeCommand::generate))));
    }

    private static int generate(CommandContext<ServerCommandSource> context) {
        CellData cell = CellArgumentType.getCell("cell", context);

        if(!cell.isEmpty()) {
            ServerCommandSource source = context.getSource();
            DimensionUtils.tryPlaceCell(source.getServer(), cell);
            source.sendMessage(Text.literal("Generating the new cell"));
        }

        return 1;
    }

}
