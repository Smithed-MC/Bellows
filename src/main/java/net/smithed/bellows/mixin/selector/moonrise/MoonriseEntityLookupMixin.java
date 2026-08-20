package net.smithed.bellows.mixin.selector.moonrise;

import ca.spottedleaf.moonrise.libs.ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.EntityLookupExtender;
import net.smithed.bellows.mixin_interface.ISimpleEntityLookupExtender;
import net.smithed.bellows.utils.NBTUtils;
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
public abstract class MoonriseEntityLookupMixin<T extends Entity> implements EntityLookupExtender<T>, ISimpleEntityLookupExtender<T> {

    @Shadow @Final protected ConcurrentChainedLong2ReferenceHashTable<Entity> entityById;
    @Shadow public abstract <U extends Entity> void get(EntityTypeTest<Entity, U> filter, AbortableIterationConsumer<U> action);

    @Unique
    private static final int REASONABLE_SEARCH_SIZE = 100;
    @Unique
    private final Map<String, Set<EntityAccess>> entityMap = new HashMap<>();

    @Override
    public void bellows_addEntityToTagMap(String tag, EntityAccess entity) {
        Set<EntityAccess> set = entityMap.computeIfAbsent(tag, k -> new HashSet<>());
        set.add(entity);
    }

    @Override
    public void bellows_removeEntityFromTagMap(String tag, EntityAccess entity) {
        Set<EntityAccess> set = entityMap.get(tag);
        if(set != null) {
            set.removeAll(Collections.singleton(entity));
        }
    }

    /**
     * @author ImCoolYeah105
     * Add entity type to map when loaded
     */
    @Inject(method = "addEntity", at = @At("TAIL"))
    private void bellows_addEntity(Entity entity, boolean fromDisk, boolean event, CallbackInfoReturnable<Boolean> ci) {
        if(entityById.containsKey(entity.getId())) {
            String name = NBTUtils.translationToTypeName(entity.getType().getDescriptionId());
            if(!name.isEmpty()) {
                this.bellows_addEntityToTagMap(name, entity);
            }
            if(!entity.entityTags().isEmpty()) {
                entity.entityTags().forEach(tag -> bellows_addEntityToTagMap(tag, entity));
            }
        }
    }

    /**
     * @author ImCoolYeah105
     * Remove entity type & tags from map when unloaded
     */
    @Inject(method = "removeEntity", at = @At("TAIL"))
    private void bellows_removeEntity(Entity entity, CallbackInfo ci) {
        if(!entityById.containsKey(entity.getId())) {
            String name = NBTUtils.translationToTypeName(entity.getType().getDescriptionId());
            if(!name.isEmpty()) {
                this.bellows_removeEntityFromTagMap(name, entity);
            }
            if(!entity.entityTags().isEmpty()) {
                entity.entityTags().forEach(tag -> bellows_removeEntityFromTagMap(tag, entity));
            }
        }
    }

    /**
     * This is a modified version of the forEach method in the base class.
     * It will check the cache for the type and tags of the selector, and use the smallest list of entities
     * retrieved for the @e search instead of all entities.
     */
    @Override
    public <U extends T> void bellows_forEachTaggedEntity(EntityTypeTest<@NotNull T, @NotNull U> filter, SelectorContainer container, AbortableIterationConsumer<@NotNull U> action) {
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
            bellows_forEachInCollection(set, filter, action);
        } else if (list != null) {
            list.forEach(iset -> bellows_forEachInCollection(iset, filter, action));
        } else {
            this.get((EntityTypeTest<Entity, U>) filter, action);
        }
    }

    @Unique
    public <U extends T> void bellows_forEachInCollection(Collection<EntityAccess> collection, EntityTypeTest<T, U> filter, AbortableIterationConsumer<U> action) {

        for (EntityAccess entity : collection) {
            Visibility visibility = EntityLookup.getEntityStatus((Entity) entity);
            if (visibility.isAccessible()) {
                U casted = filter.tryCast((T) entity);
                if (casted != null && action.accept(casted).shouldAbort()) {
                    break;
                }
            }
        }
    }

    @Override
    public EntityLookupExtender<?> bellows_getVisibleEntities() {
        return this;
    }
}
