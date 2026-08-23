package net.smithed.bellows.mixin.nbt;

import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueOutput;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;
import net.smithed.bellows.utils.ContextMutation;
import net.smithed.bellows.utils.NBTUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

    @Shadow @Final
    private static Logger LOGGER;

    @Inject(method = "lambda$bootStrap$45", at = @At("HEAD"), cancellable = true)
    private static void bellows_nbt(CompoundTag tag, boolean inverted, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Bellows.CONFIG.nbtOptimizations && entity instanceof EntityExtender mixin) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                for (String str : NBTUtils.getTopLevelPaths(tag)) {
                    if(output.buildResult().contains(str)) {
                        continue;
                    }
                    // if any attempt to get a data element fails, mark output as a failure and break out of the loop
                    if(!ContextMutation.getFilteredNbt(mixin, output, str)) {
                        output = null;
                        break;
                    }
                }
                if(output != null) {
                    cir.setReturnValue(NbtUtils.compareNbt(tag, output.buildResult(), true) != inverted);
                }
            }
        }
    }
}
