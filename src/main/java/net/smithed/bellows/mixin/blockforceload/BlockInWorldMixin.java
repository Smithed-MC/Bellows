package net.smithed.bellows.mixin.blockforceload;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.smithed.bellows.utils.ContextMutation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin {

    /**
     * Redirect normal getBlockState to getBlockStateNoLoad.
     * @param level - (from vanilla) level
     * @param pos - (from vanilla) block pos
     * @return BlockState - block state
     */
    @Redirect(method = "getState()Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState bellows_getBlockState(LevelReader level, BlockPos pos) {
        return ContextMutation.getBlockState(level, pos);
    }

    /**
     * Redirect normal getBlockEntity to getBlockEntityNoLoad.
     * @param level - (from vanilla) level
     * @param pos - (from vanilla) block pos
     * @return BlockEntity - block entity
     */
    @Redirect(method = "getEntity()Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity bellows_getBlockEntity(LevelReader level, BlockPos pos) {
        return ContextMutation.getBlockEntity(level, pos);
    }
}
