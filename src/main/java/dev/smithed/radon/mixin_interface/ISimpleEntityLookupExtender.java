package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.NotNull;

public interface ISimpleEntityLookupExtender<T extends EntityAccess> {

    EntityLookup<?> radon_getVisibleEntities();

    <U extends T> void radon_forEachTaggedEntity(EntityTypeTest<@NotNull T, @NotNull U> filter, AbortableIterationConsumer<@NotNull U> action, SelectorContainer container);

}
