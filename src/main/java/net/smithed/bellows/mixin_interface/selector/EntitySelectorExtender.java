package net.smithed.bellows.mixin_interface.selector;

import net.smithed.bellows.utils.SelectorContainer;

public interface EntitySelectorExtender {

    /**
     * Sets the type/tag container on a selector.
     * @param container - selector container
     */
    void bellows_setContainer(SelectorContainer container);
}
