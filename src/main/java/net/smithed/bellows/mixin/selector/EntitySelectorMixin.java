package net.smithed.bellows.mixin.selector;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.selector.EntitySelectorExtender;
import net.smithed.bellows.mixin_interface.selector.ServerLevelExtender;
import net.smithed.bellows.utils.SelectorContainer;
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
public abstract class EntitySelectorMixin implements EntitySelectorExtender {

    @Shadow @Final
    private EntityTypeTest<Entity, ?> type;
    @Shadow
    protected abstract int getResultLimit();

    @Unique
    private SelectorContainer bellows_container;

    /**
     * Bypass for addEntities method that redirects to type/tag cached version if possible. Cancels the original method.
     * @author ICY105
     * @param result - (from vanilla) list to add matching entities to
     * @param level - (from vanilla) level
     * @param absoluteAABB - (from vanilla) bounding box
     * @param predicate - (from vanilla) predicate
     * @param ci - callback info
     */
    @Inject(method = "addEntities", at=@At("HEAD"), cancellable = true)
    void bellows_addEntities(List<Entity> result, ServerLevel level, @Nullable AABB absoluteAABB, Predicate<Entity> predicate, CallbackInfo ci) {
        int i = this.getResultLimit();
        if (result.size() < i) {
            if(Bellows.CONFIG.entitySelectorOptimizations && level instanceof ServerLevelExtender extender) {
                if(absoluteAABB == null || !bellows_container.selectorTags.isEmpty()) {
                    extender.bellows_getEntities(this.type, predicate, result, i, bellows_container);
                } else {
                    level.getEntities(this.type, absoluteAABB, predicate, result, i);
                }
                ci.cancel();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_setContainer(SelectorContainer container) {
        this.bellows_container = container;
    }
}
