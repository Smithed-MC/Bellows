package net.smithed.bellows.mixin_interface.selector;

import net.smithed.bellows.utils.SelectorContainer;

public interface EntitySelectorParserExtender {

    /**
     * Retrieves the type/tag selector container.
     * @return SelectorContainer - selector container
     */
    SelectorContainer bellows_getSelectorContainer();
}
