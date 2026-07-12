package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IEntityIndexExtender;
import dev.smithed.radon.mixin_interface.ISimpleEntityLookupExtender;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;

@Mixin(LevelEntityGetterAdapter.class)
public abstract class LevelEntityGetterAdapterMixin<T extends EntityAccess> implements ISimpleEntityLookupExtender<T> {

    @Shadow @Final private EntityLookup<@NotNull T> visibleEntities;

    public IEntityIndexExtender<?> radon_getVisibleEntities() {
        if(visibleEntities instanceof IEntityIndexExtender<?> extender) {
            return extender;
        } else {
            return null;
        }
    }
}
