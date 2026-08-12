package dev.smithed.radon.mixin_interface.command_stack;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface EntityExtender {

    Supplier<String> radon_getPlainTextNameSupplier();
    Supplier<Component> radon_getDisplayNameSupplier();

}
