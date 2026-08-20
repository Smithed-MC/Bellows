package net.smithed.bellows.mixin_interface.selector;

import net.minecraft.world.level.entity.EntityAccess;

public interface LevelEntityGetterAdapterExtender<T extends EntityAccess> {

    EntityLookupExtender<?> bellows_getVisibleEntities();
}
