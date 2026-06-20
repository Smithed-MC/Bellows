package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.SelectorContainer;

public interface IEntitySelectorExtender {

    void setContainer(SelectorContainer container);
    SelectorContainer getContainer(SelectorContainer container);

}
