package net.smithed.bellows;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.smithed.bellows.commands.BellowsCommand;
import net.smithed.bellows.commands.BellowsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bellows implements ModInitializer {

    public static final BellowsConfig CONFIG = new BellowsConfig();
    public static final String MOD_ID = "bellows";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Bellows");
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> BellowsCommand.register(dispatcher));
    }

    /**
     * Logs some information if debug mode is on.
     * @param message - data to log
     */
    public static void logDebug(Object message) {
        CONFIG.debugLogger.logDebug(message);
    }

    /**
     * Logs a formatted string information if debug mode is on.
     * @param message - formatted string
     * @param args - string format data
     */
    public static void logDebugFormat(String message, Object ... args) {
        CONFIG.debugLogger.logDebugFormat(message, args);
    }
}
