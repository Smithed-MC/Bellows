package net.smithed.bellows.mixin.selector;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.smithed.bellows.mixin_interface.selector.EntityLookupExtender;
import net.smithed.bellows.mixin_interface.selector.LevelEntityGetterAdapterExtender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelEntityGetterAdapter.class)
public abstract class LevelEntityGetterAdapterMixin<T extends EntityAccess> implements LevelEntityGetterAdapterExtender<T> {

    @Shadow @Final private EntityLookup<@NotNull T> visibleEntities;

    public EntityLookupExtender<?> bellows_getVisibleEntities() {
        if(visibleEntities instanceof EntityLookupExtender<?> extender) {
            return extender;
        } else {
            return null;
        }
    }
}
