package dev.smithed.radon.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class RadonContextMutation {

    public static BlockEntity getBlockEntity(WorldView world, BlockPos blockPos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockEntityNoLoad(blockPos);
        else
            return world.getBlockEntity(blockPos);
    }

    public static BlockState getBlockState(WorldView world, BlockPos blockPos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockStateNoLoad(blockPos);
        else
            return world.getBlockState(blockPos);
    }

    public static NbtCompound getBlockNbtFiltered(BlockEntity blockEntity, String path) {
        NbtCompound nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && blockEntity instanceof IEntityMixin mixin)
            nbtCompound = mixin.writeNbtFiltered(new NbtCompound(), path);
        if(nbtCompound == null) {
            Radon.logDebugFormat("Failed to write nbt data at %s with %s", path, blockEntity.getClass());
            nbtCompound = blockEntity.createNbtWithIdentifyingData(blockEntity.getWorld().getRegistryManager());
        }
        Radon.logDebugFormat("Retrieved NBT for %s -> %s", blockEntity.getClass(), nbtCompound);
        return nbtCompound;
    }

    public static boolean writeBlockNbtFiltered(BlockEntity blockEntity, BlockPos pos, NbtCompound nbt, String path) {
        if (blockEntity instanceof IEntityMixin mixin) {
            BlockState blockState = blockEntity.getWorld().getBlockState(pos);
            if (mixin.readNbtFiltered(nbt, path)) {
                blockEntity.markDirty();
                blockEntity.getWorld().updateListeners(pos, blockState, blockState, 3);
                return true;
            }
        }
        Radon.logDebugFormat("Failed to read nbt %s at %s with %s", nbt, path, blockEntity.getClass());
        return false;
    }

    public static NbtCompound getDataCommandObjectNbt(NbtPathArgumentType.NbtPath nbtPath, DataCommandObject dataCommandObject) throws CommandSyntaxException {
        NbtCompound nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            nbtCompound = mixin.getNbtFiltered(nbtPath.toString());
        }

        if(nbtCompound != null) {
            return nbtCompound;
        } else {
            return dataCommandObject.getNbt();
        }
    }

    public static void setDataCommandObjectNbt(NbtPathArgumentType.NbtPath nbtPath, DataCommandObject dataCommandObject, NbtCompound nbtCompound) throws CommandSyntaxException {
        if (nbtCompound.getSize() <= 1 && Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            mixin.setNbtFiltered(nbtCompound, nbtPath.toString());
        } else {
            dataCommandObject.setNbt(nbtCompound);
        }
    }

}
