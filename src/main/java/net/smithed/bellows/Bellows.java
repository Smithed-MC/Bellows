package net.smithed.bellows;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.smithed.bellows.commands.BellowsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bellows implements ModInitializer {

    public static final BellowsConfig CONFIG = new BellowsConfig();
    public static final String MOD_ID = "bellows";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Bellows");
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> BellowsCommand.register(dispatcher));
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
            CONFIG.debugContext.sendSystemMessage(text);
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
            CONFIG.debugContext.sendSystemMessage(text);
        }
    }

    public static class BellowsConfig {

        public boolean debug = false;
        public boolean nbtOptimizations = true;
        public boolean entitySelectorOptimizations = true;
        public boolean fixBlockAccessForceload = true;

        public CommandSourceStack debugContext = null;

    }

}
