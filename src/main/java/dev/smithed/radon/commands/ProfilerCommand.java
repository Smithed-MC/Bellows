package dev.smithed.radon.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.profiling.Profiler;

public class ProfilerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) { // You can also return a LiteralCommandNode for use with possible redirects
        dispatcher.register(
                Commands.literal("profiler")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("push")
                        .then(Commands.argument("name", StringArgumentType.string()).executes(ProfilerCommand::push)))
                    .then(Commands.literal("pop").executes(ProfilerCommand::pop))
        );
    }

    public static int push(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        Profiler.get().push(name);
        return Command.SINGLE_SUCCESS;
    }

    public static int pop(CommandContext<CommandSourceStack> context) {
        Profiler.get().pop();
        return Command.SINGLE_SUCCESS;
    }
}
