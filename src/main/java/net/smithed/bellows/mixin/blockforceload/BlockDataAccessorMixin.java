package net.smithed.bellows.mixin.blockforceload;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.ArgProvider;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.smithed.bellows.utils.ContextMutation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockDataAccessor.class)
public abstract class BlockDataAccessorMixin {

    @Shadow @Final
    private static SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY;

    /**
     * Overwrites standard lambda variable to include support for not loading chunks when if block is processed
     * @reason Need a way to mixin into a static field.
     */
    @Shadow
    public static final ArgProvider.Factory<DataAccessor> PROVIDER = (arg) -> ArgProvider.create("block", () -> Commands.argument(arg, BlockPosArgument.blockPos()), (context) -> {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, arg);
        BlockEntity entity = ContextMutation.getBlockEntity(context.getSource().getLevel(), pos);
        if (entity == null) {
            throw ERROR_NOT_A_BLOCK_ENTITY.create();
        } else {
            return new BlockDataAccessor(entity, pos);
        }
    });
}
