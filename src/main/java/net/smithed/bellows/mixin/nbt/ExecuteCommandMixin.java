package net.smithed.bellows.mixin.nbt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.data.DataAccessor;
import net.smithed.bellows.utils.MixinShortcuts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    /**
     * Bypasses DataAccessor::getData to get specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(
        method = "checkMatchingData(Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_checkMatchingData(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return MixinShortcuts.getData(instance, path, original);
    }

    /**
     * Bypasses DataAccessor::getData to get specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(method = "lambda$storeData$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_storeData(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return MixinShortcuts.getData(instance, path, original);
    }
}
