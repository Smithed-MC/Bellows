package net.smithed.bellows.commands;

import net.minecraft.network.chat.Component;
import net.smithed.bellows.Bellows;

public class ActiveBellowsDebugLogger implements BellowsDebugLogger {

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebug(Object message) {
        if(message == null) {
            return;
        }

        Bellows.LOGGER.info(message.toString());
        if(Bellows.CONFIG.debugContext != null) {
            Component text = Component.literal(message.toString());
            Bellows.CONFIG.debugContext.sendSystemMessage(text);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebugFormat(String message, Object... args) {
        if(message == null) {
            return;
        }

        Bellows.LOGGER.info(String.format(message, args));
        if(Bellows.CONFIG.debugContext != null) {
            Component text = Component.literal(String.format(message, args));
            Bellows.CONFIG.debugContext.sendSystemMessage(text);
        }
    }
}
