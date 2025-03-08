package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import dev.smithed.radon.utils.RadonContextMutation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.OptionalInt;
import java.util.function.IntFunction;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    @Shadow @Final static Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION;

    /**
     * @author ImCoolYeah105
     * Redirects countPathMatches call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "countPathMatches(Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_countPathMatches(DataCommandObject object, DataCommandObject object2, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        return RadonContextMutation.getDataCommandObjectNbt(path, object);
    }

    /**
     * @author ImCoolYeah105
     * @reason Redirect absolutely refused to work here. In theory in should be fine, but...
     * Redirects executeStoreData call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Overwrite
    private static ServerCommandSource executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction<NbtElement> nbtSetter, boolean requestResult) {
        return source.mergeReturnValueConsumers((successful, returnValue) -> {
            try {
                NbtCompound nbtCompound = RadonContextMutation.getDataCommandObjectNbt(path, object);
                int i = requestResult ? returnValue : (successful ? 1 : 0);
                path.put(nbtCompound, nbtSetter.apply(i));
                object.setNbt(nbtCompound);
            } catch (CommandSyntaxException var8) {}
        }, ReturnValueConsumer::chain);
    }

    /**
     * @author ImCoolYeah105
     * @reason Redirect absolutely refused to work here. In theory in should be fine, but...
     * Redirects executeStoreData call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Overwrite
    private static OptionalInt testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
        BlockBox blockBox = BlockBox.create(start, end);
        BlockBox blockBox2 = BlockBox.create(destination, destination.add(blockBox.getDimensions()));
        BlockPos blockPos = new BlockPos(blockBox2.getMinX() - blockBox.getMinX(), blockBox2.getMinY() - blockBox.getMinY(), blockBox2.getMinZ() - blockBox.getMinZ());
        int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
        if (i > 32768) {
            throw BLOCKS_TOOBIG_EXCEPTION.create(32768, i);
        } else {
            DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();
            int j = 0;

            for(int k = blockBox.getMinZ(); k <= blockBox.getMaxZ(); ++k) {
                for(int l = blockBox.getMinY(); l <= blockBox.getMaxY(); ++l) {
                    for(int m = blockBox.getMinX(); m <= blockBox.getMaxX(); ++m) {
                        BlockPos blockPos2 = new BlockPos(m, l, k);
                        BlockPos blockPos3 = blockPos2.add(blockPos);
                        BlockState blockState = RadonContextMutation.getBlockState(world, blockPos2); //world.getBlockState(blockPos2);
                        if (!masked || !blockState.isOf(Blocks.AIR)) {
                            if (blockState != RadonContextMutation.getBlockState(world, blockPos3)) { //if (blockState != world.getBlockState(blockPos3)) {
                                return OptionalInt.empty();
                            }

                            BlockEntity blockEntity = RadonContextMutation.getBlockEntity(world, blockPos2);  //world.getBlockEntity(blockPos2);
                            BlockEntity blockEntity2 = RadonContextMutation.getBlockEntity(world, blockPos2); //world.getBlockEntity(blockPos3);
                            if (blockEntity != null) {
                                if (blockEntity2 == null) {
                                    return OptionalInt.empty();
                                }

                                if (blockEntity2.getType() != blockEntity.getType()) {
                                    return OptionalInt.empty();
                                }

                                if (!blockEntity.getComponents().equals(blockEntity2.getComponents())) {
                                    return OptionalInt.empty();
                                }

                                NbtCompound nbtCompound = blockEntity.createComponentlessNbt(dynamicRegistryManager);
                                NbtCompound nbtCompound2 = blockEntity2.createComponentlessNbt(dynamicRegistryManager);
                                if (!nbtCompound.equals(nbtCompound2)) {
                                    return OptionalInt.empty();
                                }
                            }

                            ++j;
                        }
                    }
                }
            }
            return OptionalInt.of(j);
        }
    }
}
