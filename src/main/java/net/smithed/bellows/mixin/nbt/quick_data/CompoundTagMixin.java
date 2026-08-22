package net.smithed.bellows.mixin.nbt.quick_data;

import net.minecraft.nbt.CompoundTag;
import net.smithed.bellows.mixin_interface.nbt.CompoundTagExtender;
import net.smithed.bellows.utils.QuickActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompoundTag.class)
public class CompoundTagMixin implements CompoundTagExtender {

    @Unique
    private QuickActions quickActions = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_precompileQuickActions() {
        QuickActions tag = new QuickActions((CompoundTag) (Object)this);
        if(tag.hasQuickActions()) {
            quickActions = tag;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuickActions bellows_getQuickActions() {
        return quickActions;
    }

    /**
     * Merges quick data when 2 tags are merged.
     * @author ICY105
     * @param other - tagged being merged into this one
     * @param cir - callback info
     */
    @Inject(method = "merge", at = @At("TAIL"))
    private void bellows_merge(CompoundTag other, CallbackInfoReturnable<CompoundTag> cir) {
        if(other instanceof CompoundTagExtender extender && extender.bellows_getQuickActions() != null) {
            if(quickActions == null) {
                quickActions = extender.bellows_getQuickActions();
            } else {
                quickActions.merge(extender.bellows_getQuickActions());
            }
        }
    }
}
