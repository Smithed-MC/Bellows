package net.smithed.bellows.mixin_interface.blockforceload;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelExtender {

    BlockState bellows_getBlockStateNoLoad(BlockPos pos);
    BlockEntity bellows_getBlockEntityNoLoad(BlockPos pos);
}
