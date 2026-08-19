package net.smithed.bellows.mixin_interface;

import net.smithed.bellows.utils.QuickActions;

public interface CompoundTagExtender {

    void bellows_precompileQuickActions();

    QuickActions bellows_getQuickActions();

}
