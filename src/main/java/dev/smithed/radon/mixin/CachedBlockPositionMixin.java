package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CachedBlockPosition.class)
public abstract class CachedBlockPositionMixin {
    @Shadow
    @Final
    private WorldView world;
    @Shadow
    @Final
    private BlockPos pos;

    @Redirect(method = "getBlockState()Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    public BlockState radon_getBlockState(WorldView world, BlockPos pos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockStateNoLoad(pos);
        else
            return world.getBlockState(pos);
    }

    @Redirect(method = "getBlockEntity()Lnet/minecraft/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"))
    public BlockEntity radon_getBlockEntity(WorldView world, BlockPos pos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockEntityNoLoad(pos);
        else
            return world.getBlockEntity(pos);
    }

}
