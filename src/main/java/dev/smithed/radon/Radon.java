package dev.smithed.radon;

import dev.smithed.radon.commands.RadonCommand;
import dev.smithed.radon.commands.TransformCommand;
import dev.smithed.radon.commands.TransformTagArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Radon implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("radon");
    public static final RadonConfig CONFIG = new RadonConfig();
    public static final String MOD_ID = "radon";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Radon");

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.fromNamespaceAndPath(Radon.MOD_ID, "transform"),
                TransformTagArgument.class,
                SingletonArgumentInfo.contextFree(TransformTagArgument::compoundTag)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> RadonCommand.register(dispatcher));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TransformCommand.register(dispatcher));
    }

    public static void logDebug(Object message) {
        if(CONFIG.debug && message != null)
            LOGGER.info(message.toString());
    }

    public static void logDebugFormat(String message, Object ... args) {
        if(CONFIG.debug && message != null) {
            LOGGER.info(String.format(message, args));
        }
    }

    public static class RadonConfig {

        public boolean debug = false;
        public boolean nbtOptimizations = true;
        public boolean entitySelectorOptimizations = true;
        public boolean fixBlockAccessForceload = true;

    }

}
