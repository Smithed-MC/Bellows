package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IEntityIndexExtender;
import dev.smithed.radon.mixin_interface.IMinecraftServerExtender;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import dev.smithed.radon.mixin_interface.ISimpleEntityLookupExtender;
import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerWorldExtender {

    @Shadow @Final MinecraftServer server;
    @Shadow abstract <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> filter, Predicate<? super T> predicate);
    @Shadow abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public EntityLookup<Entity> getEntityIndex() {
        if(this.getEntities() instanceof ISimpleEntityLookupExtender<?> lookup)
            return lookup.getVisibleEntities();
        else
            return null;
    }

    @Override
    public <T extends Entity> void collectEntitiesByType(EntityTypeTest<Entity, T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container) {
        IEntityIndexExtender<Entity> extender = getEntityLookupExtender();
        if (extender != null) {
            if(container.isTypeTag) {
                if (server instanceof IMinecraftServerExtender mixin)
                    container.entityTypes = mixin.getEntityTagEntries(container.type);
                if (container.entityTypes == null)
                    return;
            }

            extender.forEachTaggedEntity(filter, container, (entity) -> {
                if (predicate.test(entity)) {
                    result.add(entity);
                    if (result.size() >= limit) {
                        return AbortableIterationConsumer.Continuation.ABORT;
                    }
                }

                return AbortableIterationConsumer.Continuation.CONTINUE;
            });
        } else {
            ((ServerLevel) (Object) this).getEntities(filter, predicate, result, limit);
        }
    }

    private IEntityIndexExtender<Entity> getEntityLookupExtender() {
        EntityLookup<Entity> index = this.getEntityIndex();
        if(index instanceof IEntityIndexExtender<?> mixin)
            return (IEntityIndexExtender<Entity>) mixin;
        return null;
    }

}
