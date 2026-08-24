package net.smithed.bellows.commands;

public interface BellowsDebugLogger {

    /**
     * Logs some information if debug mode is on.
     * @param message - data to log
     */
    void logDebug(Object message);

    /**
     * Logs a formatted string information if debug mode is on.
     * @param message - formatted string
     * @param args - string format data
     */
    void logDebugFormat(String message, Object ... args);
}
