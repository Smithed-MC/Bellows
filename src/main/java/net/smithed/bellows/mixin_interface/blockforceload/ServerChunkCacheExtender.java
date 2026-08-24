package net.smithed.bellows.mixin_interface.blockforceload;

import net.minecraft.world.level.TicketStorage;

public interface ServerChunkCacheExtender {

    /**
     * Exposes the inaccessible ticket storage object.
     * @return TicketStorage - ticket storage
     */
    TicketStorage bellows_getTicketStorage();
}
