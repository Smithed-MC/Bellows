package dev.smithed.radon.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(DataCommand.class)
public abstract class DataCommandMixin {
    @Shadow @Final static SimpleCommandExceptionType MERGE_FAILED_EXCEPTION;
    @Shadow @Final static SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION;

    /**
     * @author ImCoolYeah105
     * @reason there does not appear to be a clean way to replace the getNbt() call with a getFilteredNbt(path) call
     */
    @Overwrite
    public static NbtElement getNbt(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
        //inject getNbt() -> getFilteredNbt(path)
        NbtCompound nbt = null;
        if (Radon.CONFIG.nbtOptimizations && object instanceof IDataCommandObjectMixin mixin)
            nbt = mixin.getNbtFiltered(path.toString());
        if (nbt == null)
            nbt = object.getNbt();
        Collection<NbtElement> collection = path.get(nbt);
        //END

        Iterator<NbtElement> iterator = collection.iterator();
        NbtElement nbtElement = iterator.next();
        if (iterator.hasNext()) {
            throw GET_MULTIPLE_EXCEPTION.create();
        } else {
            return nbtElement;
        }
    }

    /**
     * @author ImCoolYeah105
     * Overrides default method. Reads filtered data and cancels main function if successful.
     * Otherwise, main function runs normally.
     */
    @Inject(
            //method = "executeModify(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/context/CommandContext$ObjectType;Lcom/mojang/brigadier/context/CommandContext$ModifyOperation;Ljava/util/List;)I",
            method = "executeModify",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_executeModify(CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<NbtElement> elements, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        DataCommandObject dataCommandObject = objectType.getObject(context);
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
            NbtCompound nbtCompound = mixin.getNbtFiltered(nbtPath.toString());
            if (nbtCompound != null) {
                int i = modifier.modify(context, nbtCompound, nbtPath, elements);
                if (i != 0) {
                    if (mixin.setNbtFiltered(nbtCompound, nbtPath.toString())) {
                        context.getSource().sendFeedback(dataCommandObject::feedbackModify, true);
                        cir.setReturnValue(i);
                    }
                } else {
                    throw MERGE_FAILED_EXCEPTION.create();
                }
            }
        }
    }

    /**
     * @author ImCoolYeah105
     * Overrides default method. Reads filtered data and cancels main function if successful.
     * Otherwise, main function runs normally.
     */
    @Inject(
            method = "executeMerge(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/nbt/NbtCompound;)I",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_executeMerge(ServerCommandSource source, DataCommandObject object, NbtCompound nbt, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && object instanceof IDataCommandObjectMixin mixin) {
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbt);
            int same = 0;
            for(String topNbt: topLevelNbt) {
                NbtCompound nbtCompound = mixin.getNbtFiltered(topNbt);
                NbtCompound nbtCompound2 = nbtCompound.copy().copyFrom(nbt);
                if(nbtCompound.equals(nbtCompound2)) {
                    same += 1;
                } else {
                    if (!mixin.setNbtFiltered(nbtCompound2, topNbt))
                        return;
                }
            }
            if(same == topLevelNbt.length)
                throw MERGE_FAILED_EXCEPTION.create();
            source.sendFeedback(object::feedbackModify, true);
            cir.setReturnValue(1);
        }
    }

    /**
     * @author ImCoolYeah105
     * Overrides default method. Reads filtered data and cancels main function if successful.
     * Otherwise, main function runs normally.
     */
    @Inject(
            method = "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (Radon.CONFIG.nbtOptimizations && object instanceof IDataCommandObjectMixin mixin) {
            NbtCompound nbtCompound = mixin.getNbtFiltered(path.toString());

            int i = path.remove(nbtCompound);
            if (i == 0) {
                throw MERGE_FAILED_EXCEPTION.create();
            } else {
                boolean result = mixin.setNbtFiltered(nbtCompound,path.toString());
                if(!result)
                    return;
                source.sendFeedback(object::feedbackModify, true);
                cir.setReturnValue(i);
            }
        }

    }

    /**
     * @author ImCoolYeah105
     * Overrides default method. Reads filtered data and cancels main function if successful.
     * Otherwise, main function runs normally.
     */
    @Inject(
            method = "getValuesByPath",
            at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void radon_getValuesByPath(CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, CallbackInfoReturnable<List<NbtElement>> cir) throws CommandSyntaxException {
        DataCommandObject dataCommandObject = objectType.getObject(context);
        if (Radon.CONFIG.nbtOptimizations && dataCommandObject instanceof IDataCommandObjectMixin mixin) {
            NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "sourcePath");
            List<NbtElement> nbt = nbtPath.get(mixin.getNbtFiltered(nbtPath.toString()));
            cir.setReturnValue(nbt);
        }
    }
}