package net.smithed.bellows.mixin_interface;

import net.smithed.bellows.utils.SelectorContainer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.NotNull;

public interface IEntityIndexExtender<T extends EntityAccess> {

    void bellows_addEntityToTagMap(String tag, EntityAccess entity);
    void bellows_removeEntityFromTagMap(String tag, EntityAccess entity);
    <U extends T> void bellows_forEachTaggedEntity(EntityTypeTest<@NotNull T, @NotNull U> filter, SelectorContainer container, AbortableIterationConsumer<@NotNull U> action);
}
