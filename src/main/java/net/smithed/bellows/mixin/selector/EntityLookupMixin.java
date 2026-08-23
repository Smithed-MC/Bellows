package net.smithed.bellows.mixin.selector;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.selector.EntityExtender;
import net.smithed.bellows.mixin_interface.selector.EntityLookupExtender;
import net.smithed.bellows.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(EntityLookup.class)
public abstract class EntityLookupMixin<T extends EntityAccess> implements EntityLookupExtender<T> {

    @Shadow
    public abstract <U extends T> void getEntities(EntityTypeTest<@NotNull T, @NotNull U> type, AbortableIterationConsumer<@NotNull U> consumer);

    @Unique
    private static final int REASONABLE_SEARCH_SIZE = 100;
    @Unique
    private final Map<String, Set<EntityAccess>> entityMap = new HashMap<>();

    /**
     * Add entity type to cache when the entity is loaded.
     * @author ICY105
     * @param entity - (from vanilla)
     * @param ci - callback info
     */
    @Inject(method = "add", at = @At("HEAD"))
    private void bellows_add(T entity, CallbackInfo ci) {
        if(entity instanceof Entity entityCast && entity instanceof EntityExtender extender) {
            String id = extender.bellows_getEncodeId();
            this.bellows_addEntityToTagMap(id, entity);
            if(!entityCast.entityTags().isEmpty()) {
                entityCast.entityTags().forEach(tag -> bellows_addEntityToTagMap(tag, entity));
            }
        }
    }

    /**
     *
     * Remove entity type & tags from cache when the entity is unloaded.
     * @author ICY105
     * @param entity - (from vanilla)
     * @param ci - callback info
     */
    @Inject(method = "remove", at = @At("HEAD"))
    private void bellows_remove(T entity, CallbackInfo ci) {
        if(entity instanceof Entity entityCast && entity instanceof EntityExtender extender) {
            String id = extender.bellows_getEncodeId();
            this.bellows_removeEntityFromTagMap(id, entityCast);
            if(!entityCast.entityTags().isEmpty()) {
                entityCast.entityTags().forEach(tag -> bellows_removeEntityFromTagMap(tag, entity));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_addEntityToTagMap(String tag, EntityAccess entity) {
        Set<EntityAccess> set = entityMap.computeIfAbsent(tag, k -> new HashSet<>());
        set.add(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_removeEntityFromTagMap(String tag, EntityAccess entity) {
        Set<EntityAccess> set = entityMap.get(tag);
        if(set != null) {
            set.removeAll(Collections.singleton(entity));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <U extends T> void bellows_getTaggedEntities(EntityTypeTest<@NotNull T, @NotNull U> filter, AbortableIterationConsumer<@NotNull U> action, SelectorContainer container) {
        Set<EntityAccess> set = null;
        List<Set<EntityAccess>> list = null;
        int size = Integer.MAX_VALUE;

        for (String tag : container.selectorTags) {
            Set<EntityAccess> result = this.entityMap.get(tag);
            if(result == null) {
                size = 0;
            } else if(result.size() < size) {
                set = result;
                size = result.size();
            }

            if(size < REASONABLE_SEARCH_SIZE) {
                break;
            }
        }

        if (size == 0) {
            return;
        }

        if(size >= REASONABLE_SEARCH_SIZE) {
            if (!container.isNotType && !container.type.isBlank()) {
                if (container.isTypeTag) {
                    list = new LinkedList<>();
                    int mergeSize = 0;
                    for (String type : container.entityTypes) {
                        Set<EntityAccess> result = this.entityMap.get(type);
                        if (result != null) {
                            mergeSize += result.size();
                            list.add(result);
                        }
                    }
                    if (mergeSize < size) {
                        size = mergeSize;
                        set = null;
                    }
                } else {
                    Set<EntityAccess> result = this.entityMap.get(container.type);
                    if(result == null) {
                        size = 0;
                    } else if(result.size() < size) {
                        set = result;
                        size = result.size();
                    }
                }
            }
        }

        if (size == 0) {
            return;
        }

        Bellows.logDebugFormat("searching on %s entities for %s", size, container);

        if (set != null) {
            bellows_getEntities(set, filter, action);
        } else if (list != null) {
            list.forEach(iset -> bellows_getEntities(iset, filter, action));
        } else {
            this.getEntities(filter, action);
        }
    }

    /**
     * Mostly a copy/paste from EntityLookup:getEntities, however it accepts any collection instead of the special fast map.
     * @param collection - collection of entities
     * @param filter - (from vanilla)
     * @param consumer - (from vanilla)
     * @param <U> - (from vanilla)
     */
    @Unique
    private <U extends T> void bellows_getEntities(Collection<EntityAccess> collection, EntityTypeTest<@NotNull T, @NotNull U> filter, AbortableIterationConsumer<@NotNull U> consumer) {
        for (EntityAccess entityAccess : collection) {
            T entity = (T) entityAccess;
            U maybeEntity = filter.tryCast(entity);
            if (maybeEntity != null && consumer.accept(maybeEntity).shouldAbort()) {
                return;
            }
        }
    }
}
