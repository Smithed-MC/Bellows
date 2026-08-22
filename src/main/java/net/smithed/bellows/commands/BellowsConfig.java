package net.smithed.bellows.commands;

import net.minecraft.commands.CommandSourceStack;
import org.jspecify.annotations.NonNull;

public class BellowsConfig {

    public boolean nbtOptimizations = true;
    public boolean entitySelectorOptimizations = true;
    public boolean fixBlockAccessForceload = true;

    @NonNull
    public BellowsDebugLogger debugLogger = new EmptyBellowsDebugLogger();
    public CommandSourceStack debugContext = null;
}
