package net.smithed.bellows.mixin.blockforceload;

import net.smithed.bellows.mixin_interface.ServerChunkCacheExtender;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin implements ServerChunkCacheExtender {

    @Shadow @Final private TicketStorage ticketStorage;

    @Override
    public TicketStorage bellows_getTicketStorage() {
        return ticketStorage;
    }
}
