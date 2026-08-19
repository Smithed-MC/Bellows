package dev.smithed.radon.mixin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.utils.RadonContextMutation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(BlockDataObject.class)
public abstract class BlockDataObjectMixin implements IDataCommandObjectMixin {

    @Shadow @Final static SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION;
    @Shadow @Final BlockEntity blockEntity;
    @Shadow @Final BlockPos pos;

    /**
     * Overwrites standard lambda variable to include support for not loading chunks when if block is processed
     * @reason afaik, there is no way to inject code into a lambda
     */
    @Shadow public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = (argumentName) -> new DataCommand.ObjectType() {
        public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, argumentName + "Pos");
            BlockEntity blockEntity = RadonContextMutation.getBlockEntity(context.getSource().getWorld(), blockPos);
            if (blockEntity == null) {
                throw INVALID_BLOCK_EXCEPTION.create();
            } else {
                return new BlockDataObject(blockEntity, blockPos);
            }
        }

        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then(CommandManager.literal("block").then(argumentAdder.apply(CommandManager.argument(argumentName + "Pos", BlockPosArgumentType.blockPos()))));
        }
    };

    @Override
    public NbtCompound getNbtFiltered(String path) {
        return RadonContextMutation.getBlockNbtFiltered(this.blockEntity, path);
    }

    @Override
    public boolean setNbtFiltered(NbtCompound nbt, String path) {
        return RadonContextMutation.writeBlockNbtFiltered(this.blockEntity, this.pos, nbt, path);
    }

}
