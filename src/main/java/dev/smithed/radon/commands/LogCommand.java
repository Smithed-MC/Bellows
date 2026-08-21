package dev.smithed.radon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.smithed.radon.Radon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class LogCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) { // You can also return a LiteralCommandNode for use with possible redirects
        dispatcher.register(
            Commands.literal("echo")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("echo", StringArgumentType.string()).executes(LogCommand::log))
        );
    }

    public static int log(CommandContext<CommandSourceStack> context) {
        String count = StringArgumentType.getString(context, "echo");
        Radon.LOGGER.info(count);
        context.getSource().sendSuccess(() -> Component.literal(count), true);
        return 1;
    }
}
