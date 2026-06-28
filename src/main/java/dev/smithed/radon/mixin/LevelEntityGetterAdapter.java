package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IEntityIndexExtender;
import dev.smithed.radon.mixin_interface.ISimpleEntityLookupExtender;
import dev.smithed.radon.utils.SelectorContainer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;

@Mixin(net.minecraft.world.level.entity.LevelEntityGetterAdapter.class)
public abstract class LevelEntityGetterAdapter<T extends EntityAccess> implements ISimpleEntityLookupExtender<T> {

    @Shadow @Final private EntityLookup<@NotNull T> visibleEntities;

    @Override
    public <U extends T> void radon_forEachTaggedEntity(EntityTypeTest<@NotNull T, @NotNull U> filter, AbortableIterationConsumer<@NotNull U> action, SelectorContainer container) {
        if(this.visibleEntities instanceof IEntityIndexExtender tagged) {
            tagged.radon_forEachTaggedEntity(filter, container, action);
        } else {
            this.visibleEntities.getEntities(filter, action);
        }
    }

    public EntityLookup<@NotNull T> radon_getVisibleEntities() {
        return this.visibleEntities;
    }
}
