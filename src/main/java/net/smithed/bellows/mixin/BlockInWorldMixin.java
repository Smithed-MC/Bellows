package net.smithed.bellows.mixin;

import net.smithed.bellows.utils.ContextMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin {

    @Redirect(method = "getState()Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState bellows_getBlockState(LevelReader world, BlockPos pos) {
        return ContextMutation.getBlockState(world, pos);
    }

    @Redirect(method = "getEntity()Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity bellows_getBlockEntity(LevelReader world, BlockPos pos) {
        return ContextMutation.getBlockEntity(world, pos);
    }
}
