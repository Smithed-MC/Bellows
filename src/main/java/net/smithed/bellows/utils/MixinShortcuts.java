package net.smithed.bellows.utils;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.blockforceload.LevelExtender;
import net.smithed.bellows.mixin_interface.nbt.EntityDataAccessorExtender;

public class MixinShortcuts {

    /**
     * Redirects to EntityDataAccessorExtender::bellows_getDataFiltered if possible, otherwise falls back to vanilla method.
     * @param dataAccessor - (from vanilla)
     * @param path - (from vanilla)
     * @param original - wrapped method
     * @return CompoundTag - nbt data from accessor
     * @throws CommandSyntaxException - (from vanilla)
     */
    public static CompoundTag getData(DataAccessor dataAccessor, NbtPathArgument.NbtPath path, Operation<CompoundTag> original) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataAccessor instanceof EntityDataAccessorExtender extender) {
            CompoundTag tag = extender.bellows_getDataFiltered(path.toString());
            if(tag != null) {
                return tag;
            }
        }
        return original.call(dataAccessor);
    }

    /**
     * Redirects to EntityDataAccessorExtender::bellows_getDataFiltered if possible, otherwise falls back to vanilla method.
     * @param dataAccessor - (from vanilla)
     * @param path - (from vanilla)
     * @param original - wrapped method
     * @throws CommandSyntaxException - (from vanilla)
     */
    public static void setData(DataAccessor dataAccessor, NbtPathArgument.NbtPath path, CompoundTag compoundTag, Operation<Void> original) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataAccessor instanceof EntityDataAccessorExtender extender) {
            extender.bellows_setDataFiltered(compoundTag, path.toString());
            return;
        }
        original.call();
    }

    /**
     * Redirects to LevelExtender::bellows_getBlockStateNoLoad if possible, otherwise falls back to vanilla method.
     * @param level - (from vanilla)
     * @param blockPos - (from vanilla)
     * @param original - wrapped method
     * @return BlockState - (from vanilla)
     */
    public static BlockState getBlockState(LevelReader level, BlockPos blockPos, Operation<BlockState> original) {
        if(Bellows.CONFIG.fixBlockAccessForceload && level instanceof LevelExtender extender) {
            return extender.bellows_getBlockStateNoLoad(blockPos);
        } else {
            return original.call(blockPos);
        }
    }

    /**
     * Redirects to LevelExtender::bellows_getBlockEntityNoLoad if possible, otherwise falls back to vanilla method.
     * @param level - (from vanilla)
     * @param blockPos - (from vanilla)
     * @param original - wrapped method
     * @return BlockEntity - (from vanilla)
     */
    public static BlockEntity getBlockEntity(LevelReader level, BlockPos blockPos, Operation<BlockEntity> original) {
        if(Bellows.CONFIG.fixBlockAccessForceload && level instanceof LevelExtender extender) {
            return extender.bellows_getBlockEntityNoLoad(blockPos);
        } else {
            return original.call(blockPos);
        }
    }
}
