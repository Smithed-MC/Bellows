package net.smithed.bellows.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.smithed.bellows.Bellows;

public class BellowsCommand {

    /**
     * Registers bellows command.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("bellows")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("version").executes(BellowsCommand::version))
            .then(Commands.literal("nbt-optimizations").executes(BellowsCommand::getBellowsNbtMode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::setBellowsNbtMode)))
            .then(Commands.literal("selector-optimizations").executes(BellowsCommand::getBellowsSelectorMode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::setBellowsSelectorMode)))
            .then(Commands.literal("debug-mode").executes(BellowsCommand::getBellowsDebugMode)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::setBellowsDebugMode)))
            .then(Commands.literal("fix-block-access-forceload").executes(BellowsCommand::getBellowsBlockForceloadFix)
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(BellowsCommand::setBellowsBlockForceloadFix)))
            .then(Commands.literal("debug").redirect(dispatcher.getRoot(), BellowsCommand::debug))
        );
    }

    /**
     * Sends the current version to the command source.
     * @return int - version id
     */
    public static int version(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("version = 0.10.17"), true);
        return 1017;
    }

    /**
     * Enables debug mode and registers a callback to disable debug mode when finished.
     */
    public static CommandSourceStack debug(CommandContext<CommandSourceStack> context) {
        boolean isDebugMode = Bellows.CONFIG.debugLogger instanceof ActiveBellowsDebugLogger;
        Bellows.CONFIG.debugContext = context.getSource();
        boolean ideMode = SharedConstants.IS_RUNNING_IN_IDE;
        SharedConstants.IS_RUNNING_IN_IDE = true;
        Bellows.CONFIG.debugLogger = new ActiveBellowsDebugLogger();

        return context.getSource().withCallback((_, _) -> {
            Bellows.CONFIG.debugContext = null;
            if(!isDebugMode) {
                Bellows.CONFIG.debugLogger = new EmptyBellowsDebugLogger();
            }
            if(!ideMode) {
                SharedConstants.IS_RUNNING_IN_IDE = false;
            }
        });
    }

    /**
     * Sends current nbt mode to the command source.
     */
    public static int getBellowsNbtMode(CommandContext<CommandSourceStack> context) {
        String state = Bellows.CONFIG.nbtOptimizations ? "enabled" : "disabled";
        context.getSource().sendSuccess(() -> Component.literal("nbt optimizations are " + state), true);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sets the current nbt mode.
     */
    public static int setBellowsNbtMode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows NBT optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.nbtOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sends current selector mode to the command source.
     */
    public static int getBellowsSelectorMode(CommandContext<CommandSourceStack> context) {
        String state = Bellows.CONFIG.entitySelectorOptimizations ? "enabled" : "disabled";
        context.getSource().sendSuccess(() -> Component.literal("selector optimizations are " + state), true);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sets the current selector mode.
     */
    public static int setBellowsSelectorMode(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows Selector optimizations have been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.entitySelectorOptimizations = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sends current debug mode state to the command source.
     */
    public static int getBellowsDebugMode(CommandContext<CommandSourceStack> context) {
        String state = Bellows.CONFIG.debugLogger instanceof ActiveBellowsDebugLogger ? "enabled" : "disabled";
        context.getSource().sendSuccess(() -> Component.literal("debug mode is " + state), true);

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sets the current debug mode state.
     */
    public static int setBellowsDebugMode(CommandContext<CommandSourceStack> ctx) {
        boolean enable = BoolArgumentType.getBool(ctx, "enabled");
        Component text = Component.literal("Bellows Debug Mode has been set to: " + enable);
        Bellows.CONFIG.debugLogger = enable ? new ActiveBellowsDebugLogger() : new EmptyBellowsDebugLogger();
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sends current block forceload fix mode to the command source.
     */
    public static int getBellowsBlockForceloadFix(CommandContext<CommandSourceStack> context) {
        String state = Bellows.CONFIG.fixBlockAccessForceload ? "enabled" : "disabled";
        context.getSource().sendSuccess(() -> Component.literal("block forceload fix is " + state), true);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Sets the current block forceload fix mode.
     */
    public static int setBellowsBlockForceloadFix(CommandContext<CommandSourceStack> ctx) {
        Component text = Component.literal("Bellows fix block access forceload has been set to: " + BoolArgumentType.getBool(ctx, "enabled"));
        Bellows.CONFIG.fixBlockAccessForceload = BoolArgumentType.getBool(ctx, "enabled");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }
}
