package dev.smithed.radon.mixin_interface;

import net.minecraft.world.level.entity.EntityAccess;

public interface ISimpleEntityLookupExtender<T extends EntityAccess> {

    IEntityIndexExtender<?> radon_getVisibleEntities();

}
