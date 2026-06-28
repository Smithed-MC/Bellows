package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;

public interface IEntitySelectorExtender {

    void radon_setContainer(SelectorContainer container);
    SelectorContainer radon_getContainer(SelectorContainer container);

}
