package net.smithed.bellows.mixin_interface.blockforceload;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelExtender {

    /**
     * Bypass of Level::getBlockState and unregisters the chunk loading ticket created by accessing the block.
     * @param pos - (from vanilla)
     * @return BlockState - (from vanilla)
     */
    BlockState bellows_getBlockStateNoLoad(BlockPos pos);

    /**
     * Bypass of Level::getBlockEntity and unregisters the chunk loading ticket created by accessing the block.
     * @param pos - (from vanilla)
     * @return BlockState - (from vanilla)
     */
    BlockEntity bellows_getBlockEntityNoLoad(BlockPos pos);
}
