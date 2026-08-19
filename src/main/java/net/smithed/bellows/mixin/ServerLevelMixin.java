package net.smithed.bellows.mixin;

import net.smithed.bellows.mixin_interface.IEntityIndexExtender;
import net.smithed.bellows.mixin_interface.IMinecraftServerExtender;
import net.smithed.bellows.mixin_interface.IServerWorldExtender;
import net.smithed.bellows.mixin_interface.ISimpleEntityLookupExtender;
import net.smithed.bellows.utils.SelectorContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerWorldExtender {

    @Shadow @Final private MinecraftServer server;
    @Shadow protected abstract LevelEntityGetter<@NotNull Entity> getEntities();

    @Override
    public IEntityIndexExtender<?> bellows_getEntityIndex() {
        if(this.getEntities() instanceof ISimpleEntityLookupExtender lookup) {
            return lookup.bellows_getVisibleEntities();
        } else {
            return null;
        }
    }

    @Override
    public <T extends Entity> void bellows_collectEntitiesByType(EntityTypeTest<@NotNull Entity, @NotNull T> filter, Predicate<? super T> predicate, List<? super T> result, int limit, SelectorContainer container) {
        IEntityIndexExtender<Entity> extender = (IEntityIndexExtender<Entity>) bellows_getEntityIndex();
        if (extender != null) {
            if(container.isTypeTag) {
                if (server instanceof IMinecraftServerExtender mixin) {
                    container.entityTypes = mixin.bellows_getEntityTagEntries(container.type);
                }
                if (container.entityTypes == null) {
                    return;
                }
            }

            extender.bellows_forEachTaggedEntity(filter, container, (entity) -> {
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

    @Inject(method = "tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    public void bellows_tryAddFreshEntityWithPassengers(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValue()) {
            for(String tag: entity.entityTags()) {
                bellows_getEntityIndex().bellows_removeEntityFromTagMap(tag, entity);
            }
        }
    }
}
