package net.smithed.bellows.mixin_interface.command_stack;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface EntityExtender {

    Supplier<String> bellows_getPlainTextNameSupplier();
    Supplier<Component> bellows_getDisplayNameSupplier();
}
