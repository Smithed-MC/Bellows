package dev.smithed.radon.mixin.server.function;

import com.google.common.collect.Table;
import dev.smithed.radon.Radon;
import dev.smithed.radon.server.function.CommandQueue;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.*;

import java.util.Queue;

@Mixin(Timer.class)
public class TimerMixin<T> {
    @Shadow
    @Final
    private Queue<Timer.Event<T>> events;

    @Shadow
    @Final
    private Table<String, Long, Timer.Event<T>> eventsByName;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void processEvents(T server, long time) {
        if (Radon.CONFIG.concurrentFunctions)
            CommandQueue.clear();

        while (true) {
            Timer.Event<T> event = this.events.peek();
            if (event == null || event.triggerTime > time) {
                if (Radon.CONFIG.concurrentFunctions) {
                    Radon.LOGGER.info("Executing scheduled functions");
                    long start = System.nanoTime();
//                    CommandQueue.execute();
                    Radon.LOGGER.info("Done in " + (System.nanoTime() - start) + "ns");
                }
                return;
            }
            this.events.remove();
            this.eventsByName.remove(event.name, time);
            event.callback.call(server, (Timer<T>)(Object)this, time);
        }
    }
}
