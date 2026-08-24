package net.smithed.bellows.mixin_interface.nbt;

import net.smithed.bellows.utils.QuickActions;

public interface CompoundTagExtender {

    /**
     * Converts any data stored on the component into valid quick actions.
     */
    void bellows_precompileQuickActions();

    /**
     * Returns previously compiled quick actions.
     * @return QuickActions - compiled quick actions
     */
    QuickActions bellows_getQuickActions();
}
