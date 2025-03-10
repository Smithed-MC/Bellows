package dev.smithed.radon.mixin.server.function;

import dev.smithed.radon.Radon;
import dev.smithed.radon.server.function.CommandQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FunctionTimerCallback.class)
public class FunctionTimerCallbackMixin {
    @Shadow
    @Final
    private Identifier name;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l) {
        CommandFunctionManager commandFunctionManager = minecraftServer.getCommandFunctionManager();
        commandFunctionManager.getFunction(this.name).ifPresent((function) -> {
            var source = commandFunctionManager.getScheduledCommandSource();

            if (Radon.CONFIG.concurrentFunctions)
                CommandQueue.enqueue(() -> commandFunctionManager.execute(function, source));
            else
                commandFunctionManager.execute(function, source);
        });
    }
}
