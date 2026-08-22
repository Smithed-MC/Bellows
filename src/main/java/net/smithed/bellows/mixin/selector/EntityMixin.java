package net.smithed.bellows.mixin.selector;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.smithed.bellows.mixin_interface.selector.EntityExtender;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtender {

    @Shadow @Final
    protected abstract @Nullable String getEncodeId();

    @Unique
    private String bellows_encodeId;

    /**
     * Caches entity's encode id on init.
     * @param type - (from vanilla) entity type
     * @param level - (from vanilla) level
     * @param ci - callback info
     */
    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void bellows_init(EntityType<?> type, Level level, CallbackInfo ci) {
        bellows_encodeId = getEncodeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String bellows_getEncodeId() {
        return bellows_encodeId;
    }
}
