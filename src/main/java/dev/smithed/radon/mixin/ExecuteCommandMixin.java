package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.mixin_interface.IWorldExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;
import java.util.function.IntFunction;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {
    @Shadow @Final static Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION;

    /**
     * @author ImCoolYeah105
     * reroute getNbt() -> getNbtFiltered(path), then cancel main function is successful.
     */
    @Inject(
            method = "countPathMatches(Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_countPathMatches(DataCommandObject object, NbtPathArgumentType.NbtPath path, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if(Radon.CONFIG.nbtOptimizations && object instanceof IDataCommandObjectMixin mixin) {
            NbtCompound nbt = mixin.getNbtFiltered(path.toString());
            if(nbt != null)
                cir.setReturnValue(path.count(nbt));
        }
    }


    /**
     * @author ImCoolYeah105
     * reroute getNbt() -> getNbtFiltered(path), then cancel main function if successful.
     */
    @Inject(
            method = "executeStoreData(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;Ljava/util/function/IntFunction;Z)Lnet/minecraft/server/command/ServerCommandSource;",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction<NbtElement> nbtSetter, boolean requestResult, CallbackInfoReturnable<ServerCommandSource> cir) {
        if(Radon.CONFIG.nbtOptimizations && object instanceof IDataCommandObjectMixin mixin) {
            ServerCommandSource source2 = source.mergeReturnValueConsumers((successful, returnValue) -> {
                int i = requestResult ? returnValue : (successful ? 1 : 0);
                try {
                    NbtCompound nbtCompound = mixin.getNbtFiltered(path.toString());
                    path.put(nbtCompound, nbtSetter.apply(i));
                    if(mixin.setNbtFiltered(nbtCompound, path.toString()))
                        return;
                } catch (CommandSyntaxException ignored) {}
                // use standard method if filtered version failed
                try {
                    NbtCompound nbtCompound = object.getNbt();
                    path.put(nbtCompound, nbtSetter.apply(i));
                    object.setNbt(nbtCompound);
                } catch (CommandSyntaxException ignored) {}
            }, ReturnValueConsumer::chain);
            cir.setReturnValue(source2);
        }
    }

    @Inject(
            method = "testBlocksCondition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Z)Ljava/util/OptionalInt;",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked, CallbackInfoReturnable<OptionalInt> cir) throws CommandSyntaxException {
        if(Radon.CONFIG.fixBlockAccessForceload && world instanceof IWorldExtender mixin) {
            BlockBox blockBox = BlockBox.create(start, end);
            BlockBox blockBox2 = BlockBox.create(destination, destination.add(blockBox.getDimensions()));
            BlockPos blockPos = new BlockPos(blockBox2.getMinX() - blockBox.getMinX(), blockBox2.getMinY() - blockBox.getMinY(), blockBox2.getMinZ() - blockBox.getMinZ());
            int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
            if (i > 32768) {
                throw BLOCKS_TOOBIG_EXCEPTION.create(32768, i);
            } else {
                int j = 0;

                for(int k = blockBox.getMinZ(); k <= blockBox.getMaxZ(); ++k) {
                    for(int l = blockBox.getMinY(); l <= blockBox.getMaxY(); ++l) {
                        for(int m = blockBox.getMinX(); m <= blockBox.getMaxX(); ++m) {
                            BlockPos blockPos2 = new BlockPos(m, l, k);
                            BlockPos blockPos3 = blockPos2.add(blockPos);
                            // INJECT
                            BlockState blockState = mixin.getBlockStateNoLoad(blockPos2);
                            if (!masked || !blockState.isOf(Blocks.AIR)) {
                                // INJECT
                                if (blockState != mixin.getBlockStateNoLoad(blockPos3)) {
                                    cir.setReturnValue(OptionalInt.empty());
                                    return;
                                }

                                //INJECT
                                BlockEntity blockEntity = mixin.getBlockEntityNoLoad(blockPos2);
                                BlockEntity blockEntity2 = mixin.getBlockEntityNoLoad(blockPos3);
                                if (blockEntity != null) {
                                    if (blockEntity2 == null) {
                                        cir.setReturnValue(OptionalInt.empty());
                                        return;
                                    }

                                    if (blockEntity2.getType() != blockEntity.getType()) {
                                        cir.setReturnValue(OptionalInt.empty());
                                        return;
                                    }

                                    NbtCompound nbtCompound = blockEntity.createNbt(blockEntity.getWorld().getRegistryManager());
                                    NbtCompound nbtCompound2 = blockEntity2.createNbt(blockEntity2.getWorld().getRegistryManager());
                                    if (!nbtCompound.equals(nbtCompound2)) {
                                        cir.setReturnValue(OptionalInt.empty());
                                        return;
                                    }
                                }
                                ++j;
                            }
                        }
                    }
                }
                cir.setReturnValue(OptionalInt.of(j));
            }
        }
    }
}
