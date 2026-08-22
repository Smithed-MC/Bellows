package net.smithed.bellows.mixin.blockforceload;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smithed.bellows.utils.MixinShortcuts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    /**
     * Bypasses ServerLevel::getBlockState to get block state without force loading the chunk.
     * @author ICY105
     */
    @WrapOperation(
        method = "checkRegions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Z)Ljava/util/OptionalInt;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState bellows_checkRegions_blockState(ServerLevel instance, BlockPos blockPos, Operation<BlockState> original) {
        return MixinShortcuts.getBlockState(instance, blockPos, original);
    }

    /**
     * Bypasses ServerLevel::getBlockEntity to get block entity without force loading the chunk.
     * @author ICY105
     */
    @WrapOperation(
            method = "checkRegions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Z)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private static BlockEntity bellows_checkRegions_blockEntity(ServerLevel instance, BlockPos blockPos, Operation<BlockEntity> original) {
        return MixinShortcuts.getBlockEntity(instance, blockPos, original);
    }
}
