package net.smithed.bellows.mixin_interface.selector;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.smithed.bellows.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface ServerLevelExtender {

    /**
     * Returns the instance of EntityLookupExtender for this server level, if available.
     * @return EntityLookupExtender - instance for this level
     */
    EntityLookupExtender<?> bellows_getEntityIndex();

    /**
     * Bypass of ServerLevel::getEntities that uses the type/tag cache to speed up entity search.
     * @param filter - (from vanilla)
     * @param predicate - (from vanilla)
     * @param result - (from vanilla)
     * @param limit - (from vanilla)
     * @param container - type/tag info container
     * @param <T> - (from vanilla)
     */
    <T extends Entity> void bellows_getEntities(EntityTypeTest<@NotNull Entity, @NotNull T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container);
}
