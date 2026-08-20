package net.smithed.bellows.mixin.displaynames;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.smithed.bellows.mixin_interface.displaynames.EntityExtender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtender {

    @Shadow
    public abstract Component getName();
    @Shadow
    public abstract Component getDisplayName();

    @Override
    public Supplier<String> bellows_getPlainTextNameSupplier() {
        return () -> this.getName().getString();
    }

    @Override
    public Supplier<Component> bellows_getDisplayNameSupplier() {
        return this::getDisplayName;
    }
}
