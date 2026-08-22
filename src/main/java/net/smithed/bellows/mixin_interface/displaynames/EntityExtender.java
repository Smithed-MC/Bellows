package net.smithed.bellows.mixin_interface.displaynames;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface EntityExtender {

    /**
     * Returns a supplier that will compute this entity's plain text name when requested.
     * @return Supplier<String> - plain text name
     */
    Supplier<String> bellows_getPlainTextNameSupplier();

    /**
     * Returns a supplier that will compute this entity's display text name when requested.
     * @return Supplier<Component> - display name
     */
    Supplier<Component> bellows_getDisplayNameSupplier();
}
