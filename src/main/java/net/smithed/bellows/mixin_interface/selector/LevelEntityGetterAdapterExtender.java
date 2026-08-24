package net.smithed.bellows.mixin_interface.selector;

import net.minecraft.world.level.entity.EntityAccess;

public interface LevelEntityGetterAdapterExtender<T extends EntityAccess> {

    /**
     * Retrieves the visible entities field, if it is type EntityLookupExtender.
     * @return EntityLookupExtender - visible entities field
     */
    EntityLookupExtender<?> bellows_getVisibleEntities();
}
