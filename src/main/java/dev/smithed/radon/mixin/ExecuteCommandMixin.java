package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import dev.smithed.radon.utils.ContextMutation;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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

    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE;

    /**
     * @author ImCoolYeah105
     * Redirects countPathMatches call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "checkMatchingData(Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag bellows_countPathMatches(DataAccessor object, DataAccessor accessor, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return ContextMutation.getDataCommandObjectNbt(path, object);
    }

    /**
     * @author ImCoolYeah105
     * @reason Redirect absolutely refused to work here. In theory in should be fine, but...
     * Redirects executeStoreData call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Overwrite
    private static CommandSourceStack storeData(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path, IntFunction<Tag> nbtSetter, boolean requestResult) {
        return source.withCallback((successful, returnValue) -> {
            try {
                CompoundTag nbtCompound = ContextMutation.getDataCommandObjectNbt(path, object);
                int i = requestResult ? returnValue : (successful ? 1 : 0);
                path.set(nbtCompound, nbtSetter.apply(i));
                ContextMutation.setDataCommandObjectNbt(path, object, nbtCompound);
            } catch (CommandSyntaxException _) {}
        }, CommandResultCallback::chain);
    }

    /**
     * @author ImCoolYeah105
     * @reason Redirect absolutely refused to work here. In theory in should be fine, but...
     * Redirects executeStoreData call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Overwrite
    private static OptionalInt checkRegions(ServerLevel world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
        BoundingBox blockBox = BoundingBox.fromCorners(start, end);
        BoundingBox blockBox2 = BoundingBox.fromCorners(destination, destination.offset(blockBox.getLength()));
        BlockPos blockPos = new BlockPos(blockBox2.minX() - blockBox.minX(), blockBox2.minY() - blockBox.minY(), blockBox2.minZ() - blockBox.minZ());
        int i = blockBox.getXSpan() * blockBox.getYSpan() * blockBox.getZSpan();
        if (i > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, i);
        } else {
            RegistryAccess dynamicRegistryManager = world.registryAccess();
            int j = 0;

            for(int k = blockBox.minZ(); k <= blockBox.maxZ(); ++k) {
                for(int l = blockBox.minY(); l <= blockBox.maxY(); ++l) {
                    for(int m = blockBox.minX(); m <= blockBox.maxX(); ++m) {
                        BlockPos blockPos2 = new BlockPos(m, l, k);
                        BlockPos blockPos3 = blockPos2.offset(blockPos);
                        BlockState blockState = ContextMutation.getBlockState(world, blockPos2); //world.getBlockState(blockPos2);
                        if (!masked || !blockState.is(Blocks.AIR)) {
                            if (blockState != ContextMutation.getBlockState(world, blockPos3)) { //if (blockState != world.getBlockState(blockPos3)) {
                                return OptionalInt.empty();
                            }

                            BlockEntity blockEntity = ContextMutation.getBlockEntity(world, blockPos2);  //world.getBlockEntity(blockPos2);
                            BlockEntity blockEntity2 = ContextMutation.getBlockEntity(world, blockPos2); //world.getBlockEntity(blockPos3);
                            if (blockEntity != null) {
                                if (blockEntity2 == null) {
                                    return OptionalInt.empty();
                                }

                                if (blockEntity2.getType() != blockEntity.getType()) {
                                    return OptionalInt.empty();
                                }

                                if (!blockEntity.components().equals(blockEntity2.components())) {
                                    return OptionalInt.empty();
                                }

                                CompoundTag nbtCompound = blockEntity.saveCustomOnly(dynamicRegistryManager);
                                CompoundTag nbtCompound2 = blockEntity2.saveCustomOnly(dynamicRegistryManager);
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
