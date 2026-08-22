package net.smithed.bellows.mixin_interface.blockforceload;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelExtender {

    /**
     * Bypass of Level::getBlockState and unregisteres the chunk loading ticket created by accessing the block.
     * @param pos - (from vanilla) block position
     * @return BlockState - (from vanilla) block state
     */
    BlockState bellows_getBlockStateNoLoad(BlockPos pos);

    /**
     * Bypass of Level::getBlockEntity and unregisteres the chunk loading ticket created by accessing the block.
     * @param pos - (from vanilla) block position
     * @return BlockState - (from vanilla) block state
     */
    BlockEntity bellows_getBlockEntityNoLoad(BlockPos pos);
}
