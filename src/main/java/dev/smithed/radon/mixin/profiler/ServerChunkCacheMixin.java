package dev.smithed.radon.mixin.profiler;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Inject(method = "clearCache()V", at = @At(value = "HEAD"))
    private void radon_clearCache_start(CallbackInfo ci) {
        Profiler.get().push("clearCache");
    }

    @Inject(method = "clearCache()V", at = @At(value = "TAIL"))
    private void radon_clearCache_end(CallbackInfo ci) {
        Profiler.get().pop();
    }
}
