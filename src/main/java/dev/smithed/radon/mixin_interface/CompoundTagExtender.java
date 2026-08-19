package dev.smithed.radon.mixin_interface;

import dev.smithed.radon.utils.QuickActions;

public interface CompoundTagExtender {

    void radon_precompileQuickActions();

    QuickActions radon_getQuickActions();

}
