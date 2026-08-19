package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.QuickActions;

public interface CompoundTagExtender {

    void bellows_precompileQuickActions();

    QuickActions bellows_getQuickActions();

}
