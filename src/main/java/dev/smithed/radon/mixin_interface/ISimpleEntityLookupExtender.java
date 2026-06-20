package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;

public interface ISimpleEntityLookupExtender<T extends EntityAccess> {

    EntityLookup getVisibleEntities();
    <U extends T> void forEachTaggedEntity(EntityTypeTest<T, U> filter, AbortableIterationConsumer<U> action, SelectorContainer container);

}
