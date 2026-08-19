package net.smithed.bellows.mixin;

import net.smithed.bellows.mixin_interface.IEntityIndexExtender;
import net.smithed.bellows.mixin_interface.ISimpleEntityLookupExtender;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelEntityGetterAdapter.class)
public abstract class LevelEntityGetterAdapterMixin<T extends EntityAccess> implements ISimpleEntityLookupExtender<T> {

    @Shadow @Final private EntityLookup<@NotNull T> visibleEntities;

    public IEntityIndexExtender<?> bellows_getVisibleEntities() {
        if(visibleEntities instanceof IEntityIndexExtender<?> extender) {
            return extender;
        } else {
            return null;
        }
    }
}
