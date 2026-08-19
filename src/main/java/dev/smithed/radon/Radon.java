package dev.smithed.radon;

import com.mojang.brigadier.context.CommandContext;
import dev.smithed.radon.commands.RadonCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Radon implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("radon");
    public static final RadonConfig CONFIG = new RadonConfig();
    public static final String MOD_ID = "radon";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Radon");
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> RadonCommand.register(dispatcher));
    }

    public static void logDebug(Object message) {
        if(message == null) {
            return;
        }

        if(CONFIG.debug) {
            LOGGER.info(message.toString());
        }
        if(CONFIG.debugContext != null) {
            Component text = Component.literal(message.toString());
            CONFIG.debugContext.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    public static void logDebugFormat(String message, Object ... args) {
        if(message == null) {
            return;
        }
        if(CONFIG.debug) {
            LOGGER.info(String.format(message, args));
        }
        if(CONFIG.debugContext != null) {
            Component text = Component.literal(String.format(message, args));
            CONFIG.debugContext.getSource().getServer().getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    public static class RadonConfig {

        public boolean debug = false;
        public boolean nbtOptimizations = true;
        public boolean entitySelectorOptimizations = true;
        public boolean fixBlockAccessForceload = true;

        public CommandContext<CommandSourceStack> debugContext = null;

    }

}
