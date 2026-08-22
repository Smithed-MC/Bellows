package net.smithed.bellows.mixin.nbt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.nbt.CompoundTagExtender;
import net.smithed.bellows.mixin_interface.nbt.EntityDataAccessorExtender;
import net.smithed.bellows.utils.MixinShortcuts;
import net.smithed.bellows.utils.NBTUtils;
import net.smithed.bellows.utils.QuickActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataCommands.class)
public abstract class DataCommandsMixin {

    /**
     * Bypasses DataAccessor::getData to get specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(
        method = "getSingleTag(Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;Lnet/minecraft/server/commands/data/DataAccessor;)Lnet/minecraft/nbt/Tag;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_getSingleTag(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return MixinShortcuts.getData(instance, path, original);
    }

    /**
     * Bypasses DataAccessor::getData to get specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(
        method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_manipulateData_get(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        return MixinShortcuts.getData(instance, nbtPath, original);
    }

    /**
     * Bypasses DataAccessor::setData to set specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(
        method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private static void bellows_manipulateData_set(DataAccessor instance, CompoundTag compoundTag, Operation<Void> original, @Local(argsOnly = true) CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        MixinShortcuts.setData(instance, nbtPath, compoundTag, original);
    }

    /**
     * Bypasses DataAccessor::getData to set specified nbt path instead of all nbt data.
     * @author ICY105
     */
    @WrapOperation(
        method = "mergeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/nbt/CompoundTag;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_mergeData_get(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) CompoundTag nbt) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && instance instanceof EntityDataAccessorExtender dataExtender) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbt);
            CompoundTag nbtCompound = new CompoundTag();

            QuickActions quickActions = nbt instanceof CompoundTagExtender extender
                    ? extender.bellows_getQuickActions()
                    : null;

            for(String topNbt: topLevelNbt) {
                if(quickActions != null && quickActions.getQuickActionTags().contains(topNbt)) {
                    Bellows.logDebug("Skipping " + topNbt);
                    continue;
                }
                CompoundTag compound2 = dataExtender.bellows_getDataFiltered(topNbt);
                if(compound2.size() > 1) {
                    nbtCompound = compound2;
                    break;
                }
                nbtCompound.merge(compound2);
            }
            return nbtCompound;
        }
        return instance.getData();
    }

    /**
     * Redirects executeMerge call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     * @author ICY105
     */
    @WrapOperation(
        method = "mergeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/nbt/CompoundTag;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private static void bellows_mergeData_set(DataAccessor instance, CompoundTag compoundTag, Operation<Void> original, @Local(argsOnly = true) CompoundTag nbt) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && instance instanceof EntityDataAccessorExtender dataExtender) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(compoundTag);
            boolean success = true;

            QuickActions quickActions = nbt instanceof CompoundTagExtender extender
                    ? extender.bellows_getQuickActions()
                    : null;

            if(quickActions != null) {
                quickActions.getQuickActions().forEach(action -> {
                    Bellows.logDebug("Applying quick action");
                    action.accept(dataExtender.bellows_getContents());
                });
            }

            for(String topNbt: topLevelNbt) {
                if(quickActions != null && quickActions.getQuickActionTags().contains(topNbt)) {
                    Bellows.logDebug("Skipping 2 " + topNbt);
                    continue;
                }
                if(!dataExtender.bellows_setDataFiltered(compoundTag, topNbt)) {
                    success = false;
                    break;
                }
            }
            if(success) {
                return;
            }
        }
        instance.setData(compoundTag);
    }

    /**
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     * @author ICY105
     */
    @WrapOperation(
        method = "removeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_removeData_get(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return MixinShortcuts.getData(instance, path, original);
    }

    /**
     * Redirects executeRemove call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     * @author ICY105
     */
    @WrapOperation(
        method = "removeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private static void bellows_removeData_set(DataAccessor instance, CompoundTag compoundTag, Operation<Void> original, @Local(argsOnly = true) NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        MixinShortcuts.setData(instance, path, compoundTag, original);
    }

    /**
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     * @author ICY105
     */
    @WrapOperation(
        method = "resolveSourcePath(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;)Ljava/util/List;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag bellows_resolveSourcePath(DataAccessor instance, Operation<CompoundTag> original, @Local(argsOnly = true) CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "sourcePath");
        return MixinShortcuts.getData(instance, nbtPath, original);
    }
}