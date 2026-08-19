package dev.smithed.radon.mixin.command_stack;

import dev.smithed.radon.mixin_interface.command_stack.EntityExtender;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
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
    public Supplier<String> radon_getPlainTextNameSupplier() {
        return () -> this.getName().getString();
    }

    @Override
    public Supplier<Component> radon_getDisplayNameSupplier() {
        return this::getDisplayName;
    }
}
