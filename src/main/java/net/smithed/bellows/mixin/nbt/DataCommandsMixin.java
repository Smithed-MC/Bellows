package net.smithed.bellows.mixin.nbt;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.CompoundTagExtender;
import net.smithed.bellows.mixin_interface.EntityDataAccessorExtender;
import net.smithed.bellows.utils.ContextMutation;
import net.smithed.bellows.utils.NBTUtils;
import net.smithed.bellows.utils.QuickActions;
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
    private static CompoundTag bellows_getSingleTag(DataAccessor dataCommandObject, NbtPathArgument.NbtPath nbtPath, DataAccessor dataCommandObject2) throws CommandSyntaxException {
        return ContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag bellows_manipulateData_get(DataAccessor dataCommandObject, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType, DataCommands.DataManipulator modifier, List<Tag> elements) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        return ContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "manipulateData(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;Lnet/minecraft/server/commands/data/DataCommands$DataManipulator;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V")
    )
    private static void bellows_manipulateData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType, DataCommands.DataManipulator modifier, List<Tag> elements) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "targetPath");
        ContextMutation.setDataCommandObjectNbt(nbtPath, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeMerge call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "mergeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/nbt/CompoundTag;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag bellows_mergeData_get(DataAccessor dataCommandObject, CommandSourceStack source, DataAccessor object, CompoundTag nbt) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender dataExtender) {
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
    private static void bellows_mergeData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandSourceStack source, DataAccessor object, CompoundTag nbt) throws CommandSyntaxException {
        if (Bellows.CONFIG.nbtOptimizations && dataCommandObject instanceof EntityDataAccessorExtender dataExtender) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbtCompound);
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
                if(!dataExtender.bellows_setDataFiltered(nbtCompound, topNbt)) {
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
    private static CompoundTag bellows_removeData_get(DataAccessor dataCommandObject, CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return ContextMutation.getDataCommandObjectNbt(path, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "removeData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;setData(Lnet/minecraft/nbt/CompoundTag;)V")
    )
    private static void bellows_removeData_set(DataAccessor dataCommandObject, CompoundTag nbtCompound, CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        ContextMutation.setDataCommandObjectNbt(path, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "resolveSourcePath(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/commands/data/DataCommands$DataProvider;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/data/DataAccessor;getData()Lnet/minecraft/nbt/CompoundTag;")
    )
    private static CompoundTag bellows_resolveSourcePath(DataAccessor dataCommandObject, CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType) throws CommandSyntaxException {
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(context, "sourcePath");
        return ContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

}