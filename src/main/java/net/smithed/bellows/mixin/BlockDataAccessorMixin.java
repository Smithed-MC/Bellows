package net.smithed.bellows.mixin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.smithed.bellows.utils.ContextMutation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(BlockDataAccessor.class)
public abstract class BlockDataAccessorMixin {

    @Shadow @Final
    private static SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY;

    /**
     * Overwrites standard lambda variable to include support for not loading chunks when if block is processed
     * @reason afaik, there is no way to inject code into a lambda
     */
    @Shadow public static final Function<String, DataCommands.DataProvider> PROVIDER = (argumentName) -> new DataCommands.DataProvider() {
        public DataAccessor access(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(context, argumentName + "Pos");
            BlockEntity blockEntity = ContextMutation.getBlockEntity(context.getSource().getLevel(), blockPos);
            if (blockEntity == null) {
                throw ERROR_NOT_A_BLOCK_ENTITY.create();
            } else {
                return new BlockDataAccessor(blockEntity, blockPos);
            }
        }

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argument, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> argumentAdder) {
            return argument.then(Commands.literal("block").then(argumentAdder.apply(Commands.argument(argumentName + "Pos", BlockPosArgument.blockPos()))));
        }
    };
}
