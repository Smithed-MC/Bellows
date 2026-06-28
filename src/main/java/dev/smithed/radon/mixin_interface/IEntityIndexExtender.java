package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.NotNull;

public interface IEntityIndexExtender<T extends EntityAccess> {

    void radon_addEntityToTagMap(String tag, EntityAccess entity);

    void radon_removeEntityFromTagMap(String tag, EntityAccess entity);

    <U extends T> void radon_forEachTaggedEntity(EntityTypeTest<@NotNull T, @NotNull U> filter, SelectorContainer container, AbortableIterationConsumer<@NotNull U> action);

}
