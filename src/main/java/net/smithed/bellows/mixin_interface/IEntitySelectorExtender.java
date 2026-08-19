package net.smithed.bellows.mixin_interface;

import net.smithed.bellows.utils.SelectorContainer;

public interface IEntitySelectorExtender {

    void bellows_setContainer(SelectorContainer container);
    SelectorContainer bellows_getContainer(SelectorContainer container);
}
