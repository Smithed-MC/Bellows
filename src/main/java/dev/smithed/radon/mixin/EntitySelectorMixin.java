package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntitySelectorExtender;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import dev.smithed.radon.utils.SelectorContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements IEntitySelectorExtender {

    @Shadow private EntityTypeTest<Entity, ?> type;
    @Shadow abstract int getResultLimit();

    @Inject(method = "addEntities", at=@At("HEAD"), cancellable = true)
    void radon_addEntities(List<Entity> entities, ServerLevel world, @Nullable AABB box, Predicate<Entity> predicate, CallbackInfo ci) {
        int i = this.getResultLimit();
        if (entities.size() < i) {
            if(Radon.CONFIG.entitySelectorOptimizations && world instanceof IServerWorldExtender extender) {
                if (box != null) {
                    world.getEntities(this.type, box, predicate, entities, i);
                } else {
                    extender.collectEntitiesByType(this.type, predicate, entities, i, container);
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
