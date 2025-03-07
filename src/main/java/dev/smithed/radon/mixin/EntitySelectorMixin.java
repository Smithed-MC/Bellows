package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntitySelectorExtender;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements IEntitySelectorExtender {

    @Shadow private Box box;
    @Shadow private TypeFilter<Entity, ?> entityFilter;
    @Shadow abstract int getAppendLimit();

    @Inject(method = "appendEntitiesFromWorld", at=@At("HEAD"), cancellable = true)
    void appendEntitiesFromWorldInject(List<Entity> entities, ServerWorld world, @Nullable Box box, Predicate<Entity> predicate, CallbackInfo ci) {
        int i = this.getAppendLimit();
        if (entities.size() < i) {
            if(Radon.CONFIG.entitySelectorOptimizations && world instanceof IServerWorldExtender extender) {
                if (box != null) {
                    world.collectEntitiesByType(this.entityFilter, box, predicate, entities, i);
                } else {
                    extender.collectEntitiesByType(this.entityFilter, predicate, entities, i, container);
                }
                ci.cancel();
            }
        }
    }

    private SelectorContainer container;

    @Override
    public void setContainer(SelectorContainer container) {
        this.container = container;
    }

    @Override
    public SelectorContainer getContainer(SelectorContainer container) {
        return this.container;
    }
}
