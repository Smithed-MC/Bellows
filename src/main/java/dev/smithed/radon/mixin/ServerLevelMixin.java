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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerWorldExtender {

    @Shadow @Final private MinecraftServer server;
    @Shadow protected abstract LevelEntityGetter<@NotNull Entity> getEntities();

    @Override
    public EntityLookup<@NotNull Entity> radon_getEntityIndex() {
        if(this.getEntities() instanceof ISimpleEntityLookupExtender lookup) {
            return lookup.radon_getVisibleEntities();
        } else {
            return null;
        }
    }

    @Override
    public <T extends Entity> void radon_collectEntitiesByType(EntityTypeTest<@NotNull Entity, @NotNull T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container) {
        IEntityIndexExtender<Entity> extender = radon_getEntityLookupExtender();
        if (extender != null) {
            if(container.isTypeTag) {
                if (server instanceof IMinecraftServerExtender mixin) {
                    container.entityTypes = mixin.radon_getEntityTagEntries(container.type);
                }
                if (container.entityTypes == null) {
                    return;
                }
            }

            extender.radon_forEachTaggedEntity(filter, container, (entity) -> {
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

    @Unique
    private IEntityIndexExtender<Entity> radon_getEntityLookupExtender() {
        EntityLookup<@NotNull Entity> index = this.radon_getEntityIndex();
        if(index instanceof IEntityIndexExtender<?> mixin)
            return (IEntityIndexExtender<Entity>) mixin;
        return null;
    }
}
