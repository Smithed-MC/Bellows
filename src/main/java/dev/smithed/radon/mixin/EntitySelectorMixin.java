package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntitySelectorExtender;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import dev.smithed.radon.utils.SelectorContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow @Final private EntityTypeTest<Entity, ?> type;
    @Shadow protected abstract int getResultLimit();

    @Unique
    private SelectorContainer radon_container;

    @Inject(method = "addEntities", at=@At("HEAD"), cancellable = true)
    void radon_addEntities(List<Entity> result, ServerLevel level, @Nullable AABB absoluteAABB, Predicate<Entity> predicate, CallbackInfo ci) {
        int i = this.getResultLimit();
        if (result.size() < i) {
            if(Radon.CONFIG.entitySelectorOptimizations && level instanceof IServerWorldExtender extender) {
                if (absoluteAABB != null) {
                    level.getEntities(this.type, absoluteAABB, predicate, result, i);
                } else {
                    extender.radon_collectEntitiesByType(this.type, predicate, result, i, radon_container);
                }
                ci.cancel();
            }
        }
    }

    @Override
    public void radon_setContainer(SelectorContainer container) {
        this.radon_container = container;
    }

    @Override
    public SelectorContainer radon_getContainer(SelectorContainer container) {
        return this.radon_container;
    }
}
