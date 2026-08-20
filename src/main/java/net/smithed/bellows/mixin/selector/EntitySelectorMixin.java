package net.smithed.bellows.mixin.selector;

import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.IEntitySelectorExtender;
import net.smithed.bellows.mixin_interface.IServerWorldExtender;
import net.smithed.bellows.utils.SelectorContainer;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
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

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements IEntitySelectorExtender {

    @Shadow @Final private EntityTypeTest<Entity, ?> type;
    @Shadow protected abstract int getResultLimit();

    @Unique
    private SelectorContainer bellows_container;

    @Inject(method = "addEntities", at=@At("HEAD"), cancellable = true)
    void bellows_addEntities(List<Entity> result, ServerLevel level, @Nullable AABB absoluteAABB, Predicate<Entity> predicate, CallbackInfo ci) {
        int i = this.getResultLimit();
        if (result.size() < i) {
            if(Bellows.CONFIG.entitySelectorOptimizations && level instanceof IServerWorldExtender extender) {
                if(absoluteAABB == null || !bellows_container.selectorTags.isEmpty()) {
                    extender.bellows_collectEntitiesByType(this.type, predicate, result, i, bellows_container);
                } else {
                    level.getEntities(this.type, absoluteAABB, predicate, result, i);
                }
                ci.cancel();
            }
        }
    }

    @Override
    public void bellows_setContainer(SelectorContainer container) {
        this.bellows_container = container;
    }

    @Override
    public SelectorContainer bellows_getContainer(SelectorContainer container) {
        return this.bellows_container;
    }
}
