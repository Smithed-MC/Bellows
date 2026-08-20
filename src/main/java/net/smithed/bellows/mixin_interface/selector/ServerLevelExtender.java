package net.smithed.bellows.mixin_interface.selector;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.smithed.bellows.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface ServerLevelExtender {

    EntityLookupExtender<?> bellows_getEntityIndex();
    <T extends Entity> void bellows_collectEntitiesByType(EntityTypeTest<@NotNull Entity, @NotNull T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container);
}
