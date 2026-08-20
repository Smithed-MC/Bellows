package net.smithed.bellows.mixin_interface;

import net.minecraft.world.level.entity.EntityAccess;

public interface ISimpleEntityLookupExtender<T extends EntityAccess> {

    EntityLookupExtender<?> bellows_getVisibleEntities();
}
