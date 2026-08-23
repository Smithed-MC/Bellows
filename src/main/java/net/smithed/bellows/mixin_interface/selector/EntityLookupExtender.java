package net.smithed.bellows.mixin_interface.selector;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.smithed.bellows.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;

public interface EntityLookupExtender<T extends EntityAccess> {

    /**
     * Adds a type/tag mapping to the selector cache.
     * @param tag - tag to map to the entity
     * @param entity - entity that has the tag
     */
    void bellows_addEntityToTagMap(String tag, EntityAccess entity);

    /**
     * Removes a type/tag mapping from the selector cache.
     * @param tag - tag mapped to the entity
     * @param entity - entity that has the tag
     */
    void bellows_removeEntityFromTagMap(String tag, EntityAccess entity);

    /**
     * Bypass of the vanilla EntityLookup::getEntities method that uses a cache to locate entities by type/tag.
     * It will check the cache for the type and tags of the selector, and use the smallest list of entities
     * retrieved for the @e search instead of all entities.
     * @param filter - (from vanilla)
     * @param action - (from vanilla)
     * @param container - type/tag information from selector
     * @param <U> - (from vanilla)
     */
    <U extends T> void bellows_getTaggedEntities(EntityTypeTest<@NotNull T, @NotNull U> filter, AbortableIterationConsumer<@NotNull U> action, SelectorContainer container);
}
