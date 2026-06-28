package dev.smithed.radon.mixin_interface;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWorldExtender {

    BlockState radon_getBlockStateNoLoad(BlockPos pos);
    BlockEntity radon_getBlockEntityNoLoad(BlockPos pos);

}
