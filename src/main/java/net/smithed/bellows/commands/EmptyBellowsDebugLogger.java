package net.smithed.bellows.commands;

public class EmptyBellowsDebugLogger implements BellowsDebugLogger {

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebug(Object message) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebugFormat(String message, Object... args) {}
}
