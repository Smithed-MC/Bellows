package dev.smithed.radon.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.smithed.radon.Radon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RadonCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) { // You can also return a LiteralCommandNode for use with possible redirects
        dispatcher.register(
            Commands.literal("radon")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("version").executes(RadonCommand::version))
            .then(Commands.literal("nbt-optimizations").executes(RadonCommand::toggle_radon_nbt)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(RadonCommand::set_radon_nbt)))
            .then(Commands.literal("selector-optimizations").executes(RadonCommand::toggle_radon_selector)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(RadonCommand::set_radon_selector)))
            .then(Commands.literal("debug-mode").executes(RadonCommand::toggle_debug_mode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(RadonCommand::set_debug_mode)))
            .then(Commands.literal("fix-block-access-forceload").executes(RadonCommand::toggle_block_forceload_mode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(RadonCommand::set_block_forceload_mode)))
            .then(Commands.literal("debug").redirect(dispatcher.getRoot(), RadonCommand::debugStart))
        );
    }

    public static int version(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("version = 0.10.17"), true);
        return 1017;
    }

    public static CommandSourceStack debugStart(CommandContext<CommandSourceStack> context) {
        Radon.CONFIG.debugContext = context;
        return context.getSource().withCallback((_, _) -> Radon.CONFIG.debugContext = null);
    }

    public static int toggle_radon_nbt(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Radon.CONFIG.nbtOptimizations) {
            text = Component.literal("Disabled Radon NBT optimizations");
            Radon.CONFIG.nbtOptimizations = false;
        } else {
            text = Component.literal("Enabled Radon NBT optimizations");
            Radon.CONFIG.nbtOptimizations = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_radon_nbt(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Radon NBT optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Radon.CONFIG.nbtOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_radon_selector(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Radon.CONFIG.entitySelectorOptimizations) {
            text = Component.literal("Disabled Radon Selector optimizations");
            Radon.CONFIG.entitySelectorOptimizations = false;
        } else {
            text = Component.literal("Enabled Radon Selector optimizations");
            Radon.CONFIG.entitySelectorOptimizations = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_radon_selector(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Radon Selector optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Radon.CONFIG.entitySelectorOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_debug_mode(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Radon.CONFIG.debug) {
            text = Component.literal("Disabled Radon Debug Mode");
            Radon.CONFIG.debug = false;
        } else {
            text = Component.literal("Enabled Radon Debug Mode");
            Radon.CONFIG.debug = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_debug_mode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Radon Debug Mode has been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Radon.CONFIG.debug = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int toggle_block_forceload_mode(CommandContext<CommandSourceStack> context) {
        Component text;
        if(Radon.CONFIG.fixBlockAccessForceload) {
            text = Component.literal("Disabled Radon fix block access forceload");
            Radon.CONFIG.fixBlockAccessForceload = false;
        } else {
            text = Component.literal("Enabled Radon fix block access forceload");
            Radon.CONFIG.fixBlockAccessForceload = true;
        }
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int set_block_forceload_mode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Radon fix block access forceload has been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Radon.CONFIG.fixBlockAccessForceload = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }
}
