package dev.smithed.radon.server.function;

import dev.smithed.radon.Radon;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class CommandQueue {
    private static final ForkJoinPool pool = new ForkJoinPool(4, new Factory(), null, false);
    private static final Queue<Runnable> entries = new ArrayDeque<>();
    static class Factory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        private int threadCount = 0;

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool) {
                {
                    setName("RadonFunctionThread-" + threadCount++);
                }
            };
        }
    }

    public static void clear() {
        entries.clear();
    }

    public static void enqueue(Runnable task) {
        entries.add(task);
    }

    public static void execute() {
        while (!entries.isEmpty()) {
            pool.submit(entries.remove());
        }

        if (!pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
            Radon.LOGGER.error("Command execution timed out!");
        }
    }
}