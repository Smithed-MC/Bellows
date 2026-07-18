package dev.smithed.radon.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.EntityDataAccessorExtender;
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
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin) {
            return mixin.radon_getBlockEntityNoLoad(blockPos);
        } else {
            return world.getBlockEntity(blockPos);
        }
    }

    public static BlockState getBlockState(LevelReader world, BlockPos blockPos) {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin) {
            return mixin.radon_getBlockStateNoLoad(blockPos);
        } else {
            return world.getBlockState(blockPos);
        }
    }

    public static CompoundTag getDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject) throws CommandSyntaxException {
        CompoundTag nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender mixin) {
            nbtCompound = mixin.radon_getDataFiltered(nbtPath.toString());
        }

        if(nbtCompound != null) {
            return nbtCompound;
        } else {
            return dataCommandObject.getData();
        }
    }

    public static void setDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject, CompoundTag nbtCompound) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender mixin) {
            mixin.radon_setDataFiltered(nbtCompound, nbtPath.toString());
        } else {
            dataCommandObject.setData(nbtCompound);
        }
    }

}
