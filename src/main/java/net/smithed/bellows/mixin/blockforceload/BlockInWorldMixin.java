package net.smithed.bellows.mixin.blockforceload;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.smithed.bellows.utils.MixinShortcuts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin {

    /**
     * Redirect normal getBlockState to getBlockStateNoLoad.
     * @param instance - (from vanilla)
     * @param blockPos - (from vanilla)
     * @return BlockState - block state
     */
    @WrapOperation(method = "getState()Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState bellows_getBlockState(LevelReader instance, BlockPos blockPos, Operation<BlockState> original) {
        return MixinShortcuts.getBlockState(instance, blockPos, original);
    }

    /**
     * Redirect normal getBlockEntity to getBlockEntityNoLoad.
     * @param instance - (from vanilla)
     * @param blockPos - (from vanilla)
     * @return BlockEntity - block entity
     */
    @WrapOperation(method = "getEntity()Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity bellows_getBlockEntity(LevelReader instance, BlockPos blockPos, Operation<BlockEntity> original) {
        return MixinShortcuts.getBlockEntity(instance, blockPos, original);
    }
}
