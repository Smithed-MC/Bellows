package dev.smithed.radon.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.utils.NBTUtils;
import dev.smithed.radon.utils.RadonContextMutation;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DataCommand.class)
public abstract class DataCommandMixin {

    /**
     * @author ImCoolYeah105
     * Redirects DataCommandObject.getData() to mixin.getNbtFiltered() if possible.
     */
    @Redirect(
            method = "getNbt(Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;Lnet/minecraft/command/DataCommandObject;)Lnet/minecraft/nbt/NbtElement;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_getNbt(DataCommandObject dataCommandObject, NbtPathArgumentType.NbtPath nbtPath, DataCommandObject dataCommandObject2) throws CommandSyntaxException {
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeModify(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/command/DataCommand$ObjectType;Lnet/minecraft/server/command/DataCommand$ModifyOperation;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_executeModify_get(DataCommandObject dataCommandObject, CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<NbtElement> elements) throws CommandSyntaxException {
        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeModify call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeModify(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/command/DataCommand$ObjectType;Lnet/minecraft/server/command/DataCommand$ModifyOperation;Ljava/util/List;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;setNbt(Lnet/minecraft/nbt/NbtCompound;)V")
    )
    private static void radon_executeModify_write(DataCommandObject dataCommandObject, NbtCompound nbtCompound, CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<NbtElement> elements) throws CommandSyntaxException {
        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
        RadonContextMutation.setDataCommandObjectNbt(nbtPath, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeMerge call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeMerge(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/nbt/NbtCompound;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_executeMerge_get(DataCommandObject dataCommandObject, ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbt);
            NbtCompound nbtCompound = new NbtCompound();
            for(String topNbt: topLevelNbt) {
                NbtCompound compound2 = mixin.getNbtFiltered(topNbt);
                if(compound2.getSize() > 1) {
                    nbtCompound = compound2;
                    break;
                }
                nbtCompound.copyFrom(compound2);
            }
            return nbtCompound;
        }
        return dataCommandObject.getNbt();
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeMerge call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeMerge(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/nbt/NbtCompound;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;setNbt(Lnet/minecraft/nbt/NbtCompound;)V")
    )
    private static void radon_executeMerge_write(DataCommandObject dataCommandObject, NbtCompound nbtCompound, ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
        if (nbtCompound.getSize() == nbt.getSize() && Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbtCompound);
            for(String topNbt: topLevelNbt) {
                mixin.setNbtFiltered(nbtCompound, topNbt);
            }
            return;
        }
        dataCommandObject.setNbt(nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_executeRemove_get(DataCommandObject dataCommandObject, ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        return RadonContextMutation.getDataCommandObjectNbt(path, dataCommandObject);
    }

    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.setData() to mixin.setDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;setNbt(Lnet/minecraft/nbt/NbtCompound;)V")
    )
    private static void radon_executeRemove_write(DataCommandObject dataCommandObject, NbtCompound nbtCompound, ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        RadonContextMutation.setDataCommandObjectNbt(path, dataCommandObject, nbtCompound);
    }


    /**
     * @author ImCoolYeah105
     * Redirects executeRemove call to DataCommandObject.getData() to mixin.getDataCommandObjectNbt() if possible.
     */
    @Redirect(
            method = "getValuesByPath(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/command/DataCommand$ObjectType;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/command/DataCommandObject;getNbt()Lnet/minecraft/nbt/NbtCompound;")
    )
    private static NbtCompound radon_getValuesByPath(DataCommandObject dataCommandObject, CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType) throws CommandSyntaxException {
        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
        return RadonContextMutation.getDataCommandObjectNbt(nbtPath, dataCommandObject);
    }

}