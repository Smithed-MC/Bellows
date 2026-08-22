package net.smithed.bellows.mixin.selector.moonrise;

import ca.spottedleaf.moonrise.libs.ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.selector.EntityExtender;
import net.smithed.bellows.mixin_interface.selector.EntityLookupExtender;
import net.smithed.bellows.mixin_interface.selector.LevelEntityGetterAdapterExtender;
import net.smithed.bellows.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(EntityLookup.class)
public abstract class MoonriseEntityLookupMixin<T extends Entity> implements EntityLookupExtender<T>, LevelEntityGetterAdapterExtender<T> {

    @Shadow @Final
    protected ConcurrentChainedLong2ReferenceHashTable<Entity> entityById;
    @Shadow
    public abstract <U extends Entity> void get(EntityTypeTest<Entity, U> filter, AbortableIterationConsumer<U> action);

    @Unique
    private static final int REASONABLE_SEARCH_SIZE = 100;
    @Unique
    private final Map<String, Set<EntityAccess>> entityMap = new HashMap<>();

    /**
     * Add entity type to cache when the entity is loaded.
     * @author ICY105
     */
    @Inject(method = "addEntity", at = @At("TAIL"))
    private void bellows_addEntity(Entity entity, boolean fromDisk, boolean event, CallbackInfoReturnable<Boolean> ci) {
        if(entityById.containsKey(entity.getId()) && entity instanceof EntityExtender extender) {
            String id = extender.bellows_getEncodeId();
            this.bellows_addEntityToTagMap(id, entity);
            if(!entity.entityTags().isEmpty()) {
                entity.entityTags().forEach(tag -> bellows_addEntityToTagMap(tag, entity));
            }
        }
    }

    /**
     * Remove entity type & tags from cache when the entity is unloaded.
     * @author ICY105
     */
    @Inject(method = "removeEntity", at = @At("TAIL"))
    private void bellows_removeEntity(Entity entity, CallbackInfo ci) {
        if(!entityById.containsKey(entity.getId()) && entity instanceof EntityExtender extender) {
            String id = extender.bellows_getEncodeId();
            this.bellows_removeEntityFromTagMap(id, entity);
            if(!entity.entityTags().isEmpty()) {
                entity.entityTags().forEach(tag -> bellows_removeEntityFromTagMap(tag, entity));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_addEntityToTagMap(String tag, EntityAccess entity) {
        Set<EntityAccess> set = entityMap.computeIfAbsent(tag, _ -> new HashSet<>());
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
            this.get((EntityTypeTest<Entity, U>) filter, action);
        }
    }

    /**
     * Mostly a copy/paste from EntityLookup:getEntities, however it accepts any collection instead of the special fast map.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityLookupExtender<?> bellows_getVisibleEntities() {
        return this;
    }
}
