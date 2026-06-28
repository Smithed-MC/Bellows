package dev.smithed.radon.mixin_interface;

import net.minecraft.world.level.TicketStorage;

public interface ServerChunkCacheExtender {

    TicketStorage radon_getTicketStorage();

}
