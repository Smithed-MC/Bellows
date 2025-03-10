package dev.smithed.radon.mixin.server.function;

import dev.smithed.radon.Radon;
import dev.smithed.radon.server.function.CommandQueue;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

@Mixin(CommandFunctionManager.class)
public abstract class CommandFunctionManagerMixin {
    @Shadow
    @Final
    private static Identifier TICK_TAG_ID;

    @Unique
    private boolean active = false;

    @Inject(method = "executeAll", at = @At(value = "HEAD"))
    private void radon_executeAllStart(Collection<CommandFunction<ServerCommandSource>> functions, Identifier label, CallbackInfo ci) {
        if (Radon.CONFIG.concurrentFunctions && label.equals(TICK_TAG_ID)) {
            active = true;
            CommandQueue.clear();
        } else {
            active = false;
        }
    }

    @Redirect(method = "executeAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunctionManager;execute(Lnet/minecraft/server/function/CommandFunction;Lnet/minecraft/server/command/ServerCommandSource;)V"))
    private void radon_executeAllEnqueue(CommandFunctionManager manager, CommandFunction<ServerCommandSource> function, ServerCommandSource source) {
        if (active)
            CommandQueue.enqueue(() -> manager.execute(function, source));
        else
            manager.execute(function, source);
    }

    @Inject(method = "executeAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void radon_executeAllStop(CallbackInfo ci) {
        if (active) {
            Radon.LOGGER.info("Executing tick functions");
            long start = System.nanoTime();
            CommandQueue.execute();
            Radon.LOGGER.info("Done in " + (System.nanoTime() - start) + "ns");
        }

        Profilers.get().pop();
    }

}

