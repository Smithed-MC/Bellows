package dev.smithed.radon.mixin.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.smithed.radon.parallelised.ConcurrentCollections;
import dev.smithed.radon.parallelised.fastutil.ConcurrentLongLinkedOpenHashSet;
import dev.smithed.radon.parallelised.fastutil.Long2ObjectConcurrentHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;
import org.spongepowered.asm.mixin.*;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ChunkTicketManager.class)
public abstract class ChunkTicketManagerMixin {
    @Shadow
    @Final
    @Mutable
    Set<ChunkHolder> chunkHoldersWithPendingUpdates = ConcurrentCollections.newHashSet();
    @Shadow
    @Final
    @Mutable
    LongSet freshPlayerTicketPositions = new ConcurrentLongLinkedOpenHashSet();

    @Unique
    private final ReentrantLock lock = new ReentrantLock();

    @WrapMethod(method="purgeExpiredTickets")
    protected void purgeExpiredTickets(Operation og) {
        synchronized (lock) {
            og.call();
        }
    }

}
