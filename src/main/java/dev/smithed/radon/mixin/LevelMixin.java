package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor, AutoCloseable, IWorldExtender {

    @Shadow public abstract LevelChunk getChunk(int i, int j);
    @Shadow public abstract LevelChunk getChunkAt(BlockPos pos);
    @Shadow @Final boolean isClientSide;
    @Shadow @Final Thread thread;

    @Override
    public BlockState getBlockStateNoLoad(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunk worldChunk = this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            BlockState blockState = worldChunk.getBlockState(pos);
            if(this.getChunkSource() instanceof ServerChunkCache manager) {
                DistanceManager tickets = manager.chunkMap.getDistanceManager();
                int i = ChunkLevel.byStatus(ChunkStatus.FULL);
                tickets.removeTicket(TicketType.UNKNOWN, worldChunk.getPos(), i, worldChunk.getPos());
            }
            return blockState;
        }
    }

    @Override
    public BlockEntity getBlockEntityNoLoad(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return null;
        } else if(!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        } else {
            LevelChunk worldChunk = this.getChunkAt(pos);
            BlockEntity blockEntity = worldChunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
            if(this.getChunkSource() instanceof ServerChunkCache manager) {
                DistanceManager tickets = manager.chunkMap.getDistanceManager();
                int i = ChunkLevel.byStatus(ChunkStatus.FULL);
                tickets.removeTicket(TicketType.UNKNOWN, worldChunk.getPos(), i, worldChunk.getPos());
            }
            return blockEntity;
        }
    }

}
