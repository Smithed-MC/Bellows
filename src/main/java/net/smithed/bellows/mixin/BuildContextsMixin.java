package net.smithed.bellows.mixin;

import net.minecraft.commands.execution.tasks.BuildContexts;
import net.smithed.bellows.Bellows;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is kept here for debugging purposes. It enables Bellows debug mode to print the last run command.
 * This is excluded from the mixins list in fabric.mod.json. If you wish to enable this functionality for testing,
 * you will need to manually add it. Make sure you remove it when finished.
 */
@Mixin(BuildContexts.class)
public abstract class BuildContextsMixin<T> {

    @Shadow @Final
    private String commandInput;

    @Inject(method = "execute", at = @At(value = "HEAD"))
    private void bellows_execute(CallbackInfo ci) {
        Bellows.logDebugFormat("run: %s", commandInput);
    }
}
