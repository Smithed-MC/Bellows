package net.smithed.bellows.mixin.nbt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
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

@Mixin(NbtPredicate.class)
public abstract class NbtPredicateMixin {

    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    private CompoundTag tag;

    /**
     * Bypass for NbtPredicate::getEntityTagToCompare to get only specified nbt path instead of all nbt.
     * @param selected - (from vanilla)
     * @return CompoundTag - compound tag
     */
    @WrapOperation(
        method = "matches(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/predicates/NbtPredicate;getEntityTagToCompare(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag bellows_getEntityTagToCompare(Entity selected, Operation<CompoundTag> original) {
        if(Bellows.CONFIG.nbtOptimizations && selected instanceof EntityExtender mixin) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(selected.problemPath(), LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, selected.registryAccess());
                for (String str : NBTUtils.getTopLevelPaths(this.tag)) {
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
                   return output.buildResult();
                }
            }
        }
        return original.call(selected);
    }
}
