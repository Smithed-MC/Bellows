package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.CompoundTagExtender;
import dev.smithed.radon.utils.QuickActions;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompoundTag.class)
public class CompoundTagMixin implements CompoundTagExtender {

    @Unique
    private QuickActions quickActions = null;

    @Override
    public void bellows_precompileQuickActions() {
        QuickActions tag = new QuickActions((CompoundTag) (Object)this);
        if(tag.hasQuickActions()) {
            quickActions = tag;
        }
    }

    @Override
    public QuickActions bellows_getQuickActions() {
        return quickActions;
    }

    @Inject(method = "merge", at = @At("TAIL"))
    private void bellows_parse(CompoundTag other, CallbackInfoReturnable<CompoundTag> cir) {
        if(other instanceof CompoundTagExtender extender && extender.bellows_getQuickActions() != null) {
            if(quickActions == null) {
                quickActions = extender.bellows_getQuickActions();
            } else {
                quickActions.merge(extender.bellows_getQuickActions());
            }
        }
    }
}
