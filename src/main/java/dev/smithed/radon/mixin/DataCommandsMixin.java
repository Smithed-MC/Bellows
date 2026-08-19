package dev.smithed.radon.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.CompoundTagExtender;
import dev.smithed.radon.mixin_interface.EntityDataAccessorExtender;
import dev.smithed.radon.utils.NBTUtils;
import dev.smithed.radon.utils.QuickActions;
import dev.smithed.radon.utils.RadonContextMutation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DataCommands.class)
public abstract class DataCommandsMixin {

    /**
     * @author ImCoolYeah105
     * Redirects DataCommandObject.getData() to mixin.getNbtFiltered() if possible.
     */
    @Redirect(
            method = "getSingleTag(Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;Lnet/minecraft/server/commands/data/DataAccessor;)Lnet/minecraft/nbt/Tag;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag radon_getSingleTag(DataAccessor dataCommandObject, NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject2) throws CommandSyntaxException {
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag radon_manipulateData_get(DataAccessor dataCommandObject, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType, DataCommands.DataManipulator modifier, List<Tag> elements) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V")
    )
    private static void radon_manipulateData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType, DataCommands.DataManipulator modifier, List<Tag> elements) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        RadonContextMutation.setDataCommandObjectNbt(nbtPath, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeMerge call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "mergeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/nbt/CompoundTag;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag radon_mergeData_get(DataAccessor dataCommandObject, CommandSourceStack source, DataAccessor object, CompoundTag nbt) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender dataExtender) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbt);
            CompoundTag nbtCompound = new CompoundTag();

            QuickActions quickActions = nbt instanceof CompoundTagExtender extender
                    ? extender.radon_getQuickActions()
                    : null;

            for(String topNbt: topLevelNbt) {
                if(quickActions != null && quickActions.getQuickActionTags().contains(topNbt)) {
                    Radon.logDebug("Skipping " + topNbt);
                    continue;
                }
                CompoundTag compound2 = dataExtender.radon_getDataFiltered(topNbt);
                if(compound2.size() > 1) {
                    nbtCompound = compound2;
                    break;
                }
                nbtCompound.merge(compound2);
            }
            return nbtCompound;
        }
        return dataCommandObject.getData();
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeMerge call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "mergeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/nbt/CompoundTag;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V")
    )
    private static void radon_mergeData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandSourceStack source, DataAccessor object, CompoundTag nbt) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender dataExtender) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbtCompound);
            boolean success = true;

            QuickActions quickActions = nbt instanceof CompoundTagExtender extender
                    ? extender.radon_getQuickActions()
                    : null;

            if(quickActions != null) {
                quickActions.getQuickActions().forEach(action -> {
                    Radon.logDebug("Applying quick action");
                    action.accept(dataExtender.radon_getContents());
                });
            }

            for(String topNbt: topLevelNbt) {
                if(quickActions != null && quickActions.getQuickActionTags().contains(topNbt)) {
                    Radon.logDebug("Skipping 2 " + topNbt);
                    continue;
                }
                if(!dataExtender.radon_setDataFiltered(nbtCompound, topNbt)) {
                    success = false;
                    break;
                }
            }
            if(success) {
                return;
            }
        }
        dataCommandObject.setData(nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "removeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag radon_removeData_get(DataAccessor dataCommandObject, CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return RadonContextMutation.getDataCommandObjectNbt(path, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "removeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V")
    )
    private static void radon_removeData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        RadonContextMutation.setDataCommandObjectNbt(path, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "resolveSourcePath(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag radon_resolveSourcePath(DataAccessor dataCommandObject, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "sourcePath");
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

}