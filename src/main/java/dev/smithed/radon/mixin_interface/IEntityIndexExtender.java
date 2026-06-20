package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;

public interface IEntityIndexExtender<T extends EntityAccess> {

    void addEntityToTagMap(String tag, EntityAccess entity);

    void removeEntityFromTagMap(String tag, EntityAccess entity);

    <U extends T> void forEachTaggedEntity(EntityTypeTest<T, U> filter, SelectorContainer container, AbortableIterationConsumer<U> action);

}
