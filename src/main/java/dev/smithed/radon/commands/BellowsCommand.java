package dev.smithed.radon.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.smithed.radon.Bellows;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class BellowsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) { // You can also return a LiteralCommandNode for use with possible redirects
        dispatcher.register(
            Commands.literal("bellows")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("version").executes(BellowsCommand::version))
            .then(Commands.literal("nbt-optimizations").executes(BellowsCommand::toggle_bellows_nbt)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::set_bellows_nbt)))
            .then(Commands.literal("selector-optimizations").executes(BellowsCommand::toggle_bellows_selector)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::set_bellows_selector)))
            .then(Commands.literal("debug-mode").executes(BellowsCommand::toggle_debug_mode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::set_debug_mode)))
            .then(Commands.literal("fix-block-access-forceload").executes(BellowsCommand::toggle_block_forceload_mode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::set_block_forceload_mode)))
            .then(Commands.literal("debug").redirect(dispatcher.getRoot(), BellowsCommand::debugStart))
        );
    }

    public static int version(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("version = 0.10.17"), true);
        return 1017;
    }

    public static CommandSourceStack debugStart(CommandContext<CommandSourceStack> context) {
        Bellows.CONFIG.debugContext = context;
        return context.getSource().withCallback((_, _) -> Bellows.CONFIG.debugContext = null);
    }

    public static int toggle_bellows_nbt(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Bellows.CONFIG.nbtOptimizations) {
            text = Component.literal("Disabled bellows NBT optimizations");
            Bellows.CONFIG.nbtOptimizations = false;
        } else {
            text = Component.literal("Enabled bellows NBT optimizations");
            Bellows.CONFIG.nbtOptimizations = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_bellows_nbt(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows NBT optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.nbtOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_bellows_selector(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Bellows.CONFIG.entitySelectorOptimizations) {
            text = Component.literal("Disabled bellows Selector optimizations");
            Bellows.CONFIG.entitySelectorOptimizations = false;
        } else {
            text = Component.literal("Enabled bellows Selector optimizations");
            Bellows.CONFIG.entitySelectorOptimizations = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_bellows_selector(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows Selector optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.entitySelectorOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_debug_mode(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Bellows.CONFIG.debug) {
            text = Component.literal("Disabled bellows Debug Mode");
            Bellows.CONFIG.debug = false;
        } else {
            text = Component.literal("Enabled bellows Debug Mode");
            Bellows.CONFIG.debug = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_debug_mode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows Debug Mode has been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.debug = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_block_forceload_mode(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Bellows.CONFIG.fixBlockAccessForceload) {
            text = Component.literal("Disabled bellows fix block access forceload");
            Bellows.CONFIG.fixBlockAccessForceload = false;
        } else {
            text = Component.literal("Enabled bellows fix block access forceload");
            Bellows.CONFIG.fixBlockAccessForceload = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_block_forceload_mode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows fix block access forceload has been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.fixBlockAccessForceload = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }
}
