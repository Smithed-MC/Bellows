package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;

public interface IServerWorldExtender {

    EntityLookup<?> getEntityIndex();

    <T extends Entity> void collectEntitiesByType(EntityTypeTest<Entity, T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container);

}
