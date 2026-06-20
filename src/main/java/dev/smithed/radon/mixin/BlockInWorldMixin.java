package dev.smithed.radon.mixin;

import dev.smithed.radon.utils.RadonContextMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin {

    @Shadow
    @Final
    private LevelReader level;
    @Shadow
    @Final
    private BlockPos pos;

    @Redirect(method = "getState()Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState radon_getBlockState(LevelReader world, BlockPos pos) {
        return RadonContextMutation.getBlockState(world, pos);
    }

    @Redirect(method = "getEntity()Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity radon_getBlockEntity(LevelReader world, BlockPos pos) {
        return RadonContextMutation.getBlockEntity(world, pos);
    }
}
