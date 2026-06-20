package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import net.minecraft.commands.execution.tasks.BuildContexts;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is kept here for debugging purposes. It enables Radon debug mode to print the last run command.
 * This is excluded from the mixins list in fabric.mod.json. If you wish to enable this functionality for testing,
 * you will need to manually add it. Make sure you remove it when finished.
 */
@Mixin(BuildContexts.class)
public abstract class BuildContextsMixin<T> {

    @Shadow @Final String commandInput;

    @Inject(method = "execute", at = @At(value = "HEAD"))
    private void radon_execute(CallbackInfo ci) {
        Radon.logDebugFormat("run: %s", commandInput);
    }


}
