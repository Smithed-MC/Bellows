package dev.smithed.radon.mixin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
    /**
     * Overwrites standard lambda variable to include support for not loading chunks when if block is processed
     * @reason afaik, there is no way to inject code into a lambda
     */
    @Shadow public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = (argumentName) -> new DataCommand.ObjectType() {
        public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, argumentName + "Pos");
            BlockEntity blockEntity;
            if(Radon.CONFIG.fixBlockAccessForceload && context.getSource().getWorld() instanceof IWorldExtender mixin)
                blockEntity = mixin.getBlockEntityNoLoad(blockPos);
            else
                blockEntity = context.getSource().getWorld().getBlockEntity(blockPos);
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

    @Shadow @Final BlockEntity blockEntity;
    @Shadow @Final BlockPos pos;

    @Override
    public NbtCompound getNbtFiltered(String path) {
        NbtCompound nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && this.blockEntity instanceof IEntityMixin mixin)
            nbtCompound = mixin.writeNbtFiltered(new NbtCompound(), path, this.blockEntity.getWorld().getRegistryManager());
        if(nbtCompound == null) {
            Radon.logDebugFormat("Failed to write nbt data at %s with %s", path, this.blockEntity.getClass());
            nbtCompound = this.blockEntity.createNbtWithIdentifyingData(this.blockEntity.getWorld().getRegistryManager());
        }
        Radon.logDebugFormat("Retrieved NBT for %s -> %s", this.blockEntity.getClass(), nbtCompound);
        return nbtCompound;
    }

    @Override
    public boolean setNbtFiltered(NbtCompound nbt, String path) {
        if(this.blockEntity instanceof IEntityMixin mixin) {
            BlockState blockState = this.blockEntity.getWorld().getBlockState(this.pos);
            if(mixin.readNbtFiltered(nbt, path, this.blockEntity.getWorld().getRegistryManager())) {
                this.blockEntity.markDirty();
                this.blockEntity.getWorld().updateListeners(this.pos, blockState, blockState, 3);
                return true;
            }
        }
        Radon.logDebugFormat("Failed to read nbt %s at %s with %s", nbt, path, this.blockEntity.getClass());
        return false;
    }

}
