package dev.smithed.radon.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataAccessorMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RadonContextMutation {

    public static BlockEntity getBlockEntity(LevelReader world, BlockPos blockPos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockEntityNoLoad(blockPos);
        else
            return world.getBlockEntity(blockPos);
    }

    public static BlockState getBlockState(LevelReader world, BlockPos blockPos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin)
            return mixin.getBlockStateNoLoad(blockPos);
        else
            return world.getBlockState(blockPos);
    }

    public static CompoundTag getBlockNbtFiltered(BlockEntity blockEntity, String path) {
        CompoundTag nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && blockEntity instanceof IEntityMixin mixin)
            nbtCompound = mixin.saveWithoutIdFiltered(new CompoundTag(), path);
        if(nbtCompound == null) {
            Radon.logDebugFormat("Failed to write nbt data at %s with %s", path, blockEntity.getClass());
            nbtCompound = blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess());
        }
        Radon.logDebugFormat("Retrieved NBT for %s -> %s", blockEntity.getClass(), nbtCompound);
        return nbtCompound;
    }

    public static boolean writeBlockNbtFiltered(BlockEntity blockEntity, BlockPos pos, CompoundTag nbt, String path) {
        if (blockEntity instanceof IEntityMixin mixin) {
            BlockState blockState = blockEntity.getLevel().getBlockState(pos);
            if (mixin.loadFiltered(nbt, path)) {
                blockEntity.setChanged();
                blockEntity.getLevel().sendBlockUpdated(pos, blockState, blockState, 3);
                return true;
            }
        }
        Radon.logDebugFormat("Failed to read nbt %s at %s with %s", nbt, path, blockEntity.getClass());
        return false;
    }

    public static CompoundTag getDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject) throws CommandSyntaxException {
        CompoundTag nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataAccessorMixin mixin) {
            nbtCompound = mixin.getNbtFiltered(nbtPath.toString());
        }

        if(nbtCompound != null) {
            return nbtCompound;
        } else {
            return dataCommandObject.getData();
        }
    }

    public static void setDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject, CompoundTag nbtCompound) throws CommandSyntaxException {
        if (nbtCompound.size() <= 1 && Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataAccessorMixin mixin) {
            mixin.setNbtFiltered(nbtCompound, nbtPath.toString());
        } else {
            dataCommandObject.setData(nbtCompound);
        }
    }

}
