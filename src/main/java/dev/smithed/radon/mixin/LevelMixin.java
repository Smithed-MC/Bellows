package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IWorldExtender;
import dev.smithed.radon.mixin_interface.ServerChunkCacheExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor, AutoCloseable, IWorldExtender {

    @Shadow @Final private boolean isClientSide;
    @Shadow @Final private Thread thread;
    @Shadow @NotNull public abstract LevelChunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract LevelChunk getChunkAt(BlockPos pos);

    @Override
    public BlockState bellows_getBlockStateNoLoad(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            LevelChunk worldChunk = this.getChunk(chunkPos.x(), chunkPos.z());
            BlockState blockState = worldChunk.getBlockState(pos);
            if(this.getChunkSource() instanceof ServerChunkCacheExtender manager) {
                int targetTicketLevel = ChunkLevel.byStatus(ChunkStatus.FULL);
                manager.bellows_getTicketStorage().removeTicket(new Ticket(TicketType.UNKNOWN, targetTicketLevel), chunkPos);
            }
            return blockState;
        }
    }

    @Override
    public BlockEntity bellows_getBlockEntityNoLoad(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return null;
        } else if(!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        } else {
            ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            LevelChunk worldChunk = this.getChunkAt(pos);
            BlockEntity blockEntity = worldChunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
            if(this.getChunkSource() instanceof ServerChunkCacheExtender manager) {
                int targetTicketLevel = ChunkLevel.byStatus(ChunkStatus.FULL);
                manager.bellows_getTicketStorage().removeTicket(new Ticket(TicketType.UNKNOWN, targetTicketLevel), chunkPos);
            }
            return blockEntity;
        }
    }
}
