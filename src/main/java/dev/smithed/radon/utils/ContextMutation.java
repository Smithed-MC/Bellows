package dev.smithed.radon.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Bellows;
import dev.smithed.radon.mixin_interface.EntityDataAccessorExtender;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ContextMutation {

    public static BlockEntity getBlockEntity(LevelReader world, BlockPos blockPos) {
        if(Bellows.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin) {
            return mixin.bellows_getBlockEntityNoLoad(blockPos);
        } else {
            return world.getBlockEntity(blockPos);
        }
    }

    public static BlockState getBlockState(LevelReader world, BlockPos blockPos) {
        if(Bellows.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin) {
            return mixin.bellows_getBlockStateNoLoad(blockPos);
        } else {
            return world.getBlockState(blockPos);
        }
    }

    public static CompoundTag getDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject) throws CommandSyntaxException {
        CompoundTag nbtCompound = null;
        if (Bellows.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender mixin) {
            nbtCompound = mixin.bellows_getDataFiltered(nbtPath.toString());
        }

        if(nbtCompound != null) {
            return nbtCompound;
        } else {
            return dataCommandObject.getData();
        }
    }

    public static void setDataCommandObjectNbt(NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject, CompoundTag nbtCompound) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender mixin) {
            mixin.bellows_setDataFiltered(nbtCompound, nbtPath.toString());
        } else {
            dataCommandObject.setData(nbtCompound);
        }
    }

}
