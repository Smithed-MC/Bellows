package net.smithed.bellows.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.blockforceload.LevelExtender;
import net.smithed.bellows.mixin_interface.nbt.EntityDataAccessorExtender;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;

public class ContextMutation {

    /**
     * Redirects to LevelExtender::bellows_getBlockStateNoLoad if possible, otherwise falls back to vanilla method.
     * @param world - (from vanilla) level to get block from
     * @param blockPos - (from vanilla) pos of block
     * @return BlockState - (from vanilla) block state at location
     */
    public static BlockState getBlockState(LevelReader world, BlockPos blockPos) {
        if(Bellows.CONFIG.fixBlockAccessForceload && world instanceof LevelExtender extender) {
            return extender.bellows_getBlockStateNoLoad(blockPos);
        } else {
            return world.getBlockState(blockPos);
        }
    }

    /**
     * Redirects to LevelExtender::bellows_getBlockEntityNoLoad if possible, otherwise falls back to vanilla method.
     * @param world - (from vanilla) level to get block from
     * @param blockPos - (from vanilla) pos of block
     * @return BlockEntity - (from vanilla) block entity at location
     */
    public static BlockEntity getBlockEntity(LevelReader world, BlockPos blockPos) {
        if(Bellows.CONFIG.fixBlockAccessForceload && world instanceof LevelExtender extender) {
            return extender.bellows_getBlockEntityNoLoad(blockPos);
        } else {
            return world.getBlockEntity(blockPos);
        }
    }

    /**
     * Redirects to EntityDataAccessorExtender::bellows_getDataFiltered if possible, otherwise falls back to vanilla method.
     * @param nbtPath - (from vanilla) nbt path
     * @param dataAccessor - (from vanilla) data accessor
     * @return BlockEntity - (from vanilla) block entity at location
     */
    public static CompoundTag getData(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
        CompoundTag nbtCompound = null;
        if (Bellows.CONFIG.nbtOptimizations && dataAccessor instanceof EntityDataAccessorExtender extender) {
            nbtCompound = extender.bellows_getDataFiltered(nbtPath.toString());
        }

        if(nbtCompound != null) {
            return nbtCompound;
        } else {
            return dataAccessor.getData();
        }
    }

    /**
     * Redirects to EntityDataAccessorExtender::bellows_getDataFiltered if possible, otherwise falls back to vanilla method.
     * @param nbtPath - (from vanilla) nbt path
     * @param dataAccessor - (from vanilla) data accessor
     * @param nbtCompound - (from vanilla) nbt to store to entity
     */
    public static void setData(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor, CompoundTag nbtCompound) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataAccessor instanceof EntityDataAccessorExtender extender) {
            extender.bellows_setDataFiltered(nbtCompound, nbtPath.toString());
        } else {
            dataAccessor.setData(nbtCompound);
        }
    }

    /**
     * Retrieves filtered nbt data from an entity. Includes the "SelectedItem" special case for players.
     * @param extender - entity extender
     * @param output - output to store data to
     * @param path - nbt path
     * @return boolean - true if nbt path was valid, otherwise false
     */
    public static boolean getFilteredNbt(EntityExtender extender, TagValueOutput output, String path) {
        if (extender instanceof Player player && path.startsWith("SelectedItem") && isPathSelectedItem(path)) {
            ItemStack selected = player.getInventory().getSelectedItem();
            if (!selected.isEmpty()) {
                output.store("SelectedItem", ItemStack.CODEC, selected);
            }
            return true;
        } else {
            return extender.bellows_saveWithoutIdFiltered(output, path);
        }
    }

    /**
     * Determines if path is "SelectedItem" exactly.
     * @param path - nbt path
     * @return boolean - true if "SelectedItem", false otherwise (ie. "SelectedItemSlot")
     */
    private static boolean isPathSelectedItem(String path) {
        char slot = path.length() < 13 ? ' ' : path.charAt(12);
        return slot == ' ' || slot == '.' || slot == '{' || slot == '[';
    }
}
