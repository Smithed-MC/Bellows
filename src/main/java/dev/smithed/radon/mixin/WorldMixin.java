package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, IWorldExtender {

    @Shadow public abstract WorldChunk getChunk(int i, int j);
    @Shadow public abstract WorldChunk getWorldChunk(BlockPos pos);
    @Shadow @Final boolean isClient;
    @Shadow @Final Thread thread;

    @Override
    public BlockState getBlockStateNoLoad(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        } else {
            WorldChunk worldChunk = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
//            if(this.getChunkManager() instanceof ServerChunkManager manager) {
//                ChunkTicketManager tickets = manager.chunkLoadingManager.getTicketManager();
//                int level = 33 + ChunkStatus.(ChunkStatus.FULL);
//                tickets.removeTicketWithLevel(ChunkTicketType.UNKNOWN, worldChunk.getPos(), level, worldChunk.getPos());
//            }
            return worldChunk.getBlockState(pos);
        }
    }

    @Override
    public BlockEntity getBlockEntityNoLoad(BlockPos pos) {
        if (this.isOutOfHeightLimit(pos)) {
            return null;
        } else if(!this.isClient && Thread.currentThread() != this.thread) {
            return null;
        } else {
            WorldChunk worldChunk = this.getWorldChunk(pos);
            BlockEntity blockEntity = worldChunk.getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
//            if(this.getChunkManager() instanceof ServerChunkManager manager) {
//                ChunkTicketManager tickets = manager.chunkLoadingManager.getTicketManager();
//                int level = 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.FULL);
//                tickets.removeTicketWithLevel(ChunkTicketType.UNKNOWN, worldChunk.getPos(), level, worldChunk.getPos());
//            }
            return blockEntity;
        }
    }

}
