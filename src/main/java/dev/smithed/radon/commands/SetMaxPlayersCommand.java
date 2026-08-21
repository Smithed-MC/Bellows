package dev.smithed.radon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;

public class SetMaxPlayersCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) { // You can also return a LiteralCommandNode for use with possible redirects
        dispatcher.register(
            Commands.literal("set-max-players")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(SetMaxPlayersCommand::setMaxPlayers))
        );
    }

    public static int setMaxPlayers(CommandContext<CommandSourceStack> context) {
        if(context.getSource().getServer() instanceof DedicatedServer server) {
            int count = IntegerArgumentType.getInteger(context, "count");
            server.setMaxPlayers(count);
            context.getSource().sendSuccess(() -> Component.literal("set max players to " + count), true);
        } else {
            context.getSource().sendFailure(Component.literal("Not on a dedicated server"));
        }
        return 1010;
    }
}
