package dev.smithed.radon.mixin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.mixin_interface.IDataAccessorMixin;
import dev.smithed.radon.utils.RadonContextMutation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockDataAccessor.class)
public abstract class BlockDataAccessorMixin implements IDataAccessorMixin {

    @Shadow @Final static SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY;
    @Shadow @Final BlockEntity entity;
    @Shadow @Final BlockPos pos;

    /**
     * Overwrites standard lambda variable to include support for not loading chunks when if block is processed
     * @reason afaik, there is no way to inject code into a lambda
     */
    @Shadow public static final Function<String, DataCommands.DataProvider> PROVIDER = (argumentName) -> new DataCommands.DataProvider() {
        public DataAccessor access(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(context, argumentName + "Pos");
            BlockEntity blockEntity = RadonContextMutation.getBlockEntity(context.getSource().getLevel(), blockPos);
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

    @Override
    public CompoundTag getNbtFiltered(String path) {
        return RadonContextMutation.getBlockNbtFiltered(this.entity, path);
    }

    @Override
    public boolean setNbtFiltered(CompoundTag nbt, String path) {
        return RadonContextMutation.writeBlockNbtFiltered(this.entity, this.pos, nbt, path);
    }

}
