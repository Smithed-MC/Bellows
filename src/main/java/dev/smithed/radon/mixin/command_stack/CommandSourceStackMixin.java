package dev.smithed.radon.mixin.command_stack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.smithed.radon.mixin_interface.command_stack.EntityExtender;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    @Shadow
    private String textName;
    @Shadow
    private Component displayName;

    @Shadow @Final
    private CommandSource source;
    @Shadow @Final
    private Vec3 worldPosition;
    @Shadow @Final
    private ServerLevel level;
    @Shadow @Final
    private PermissionSet permissions;
    @Shadow @Final
    private MinecraftServer server;
    @Shadow @Final
    private boolean silent;
    @Shadow @Final
    private Entity entity;
    @Shadow @Final
    private CommandResultCallback resultCallback;
    @Shadow @Final
    private EntityAnchorArgument.Anchor anchor;
    @Shadow @Final
    private Vec2 rotation;
    @Shadow @Final
    private CommandSigningContext signingContext;
    @Shadow @Final
    private TaskChainer chatMessageChainer;

    @Unique
    private Supplier<String> radon_textNameSupplier = null;
    @Unique
    private Supplier<Component> radon_displayNameSupplier = null;

    @Inject(method = "withEntity(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/commands/CommandSourceStack;", at = @At("HEAD"), cancellable = true)
    public void withEntity(Entity entity, CallbackInfoReturnable<CommandSourceStack> cir) {
        if(entity instanceof EntityExtender extender && this.entity != entity) {
            CommandSourceStack newStack = new CommandSourceStack(
                    this.source,
                    this.worldPosition,
                    this.rotation,
                    this.level,
                    this.permissions,
                    null,
                    null,
                    this.server,
                    entity,
                    this.silent,
                    this.resultCallback,
                    this.anchor,
                    this.signingContext,
                    this.chatMessageChainer
            );
            radon_setSuppliers(newStack, extender);
            cir.setReturnValue(newStack);
        }
    }

    @Unique
    private CommandSourceStack radon_setSuppliers(CommandSourceStack stack, EntityExtender entity) {
        CommandSourceStackMixin stackMixin = (CommandSourceStackMixin)(Object)stack;
        stackMixin.radon_textNameSupplier = entity.bellows_getPlainTextNameSupplier();
        stackMixin.radon_displayNameSupplier = entity.bellows_getDisplayNameSupplier();
        return stack;
    }

    @Inject(method = "getTextName()Ljava/lang/String;", at = @At("HEAD"))
    public void radon_getTextName(CallbackInfoReturnable<Component> cir) {
        if(textName == null) {
            textName = radon_textNameSupplier.get();
        }
    }

    @Inject(method = "getDisplayName()Lnet/minecraft/network/chat/Component;", at = @At("HEAD"))
    public void radon_getDisplayName(CallbackInfoReturnable<Component> cir) {
        if(displayName == null) {
            displayName = radon_displayNameSupplier.get();
        }
    }

    @WrapOperation(
            method = "withSource(Lnet/minecraft/commands/CommandSource;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withSource(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withPosition(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withPosition(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withRotation(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withRotation(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withCallback(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withSuppressedOutput()Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withSuppressedOutput(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withPermission(Lnet/minecraft/server/permissions/PermissionSet;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withPermission(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withAnchor(Lnet/minecraft/commands/arguments/EntityAnchorArgument$Anchor;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withAnchor(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withLevel(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withLevel(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @WrapOperation(
            method = "withSigningContext(Lnet/minecraft/commands/CommandSigningContext;Lnet/minecraft/util/TaskChainer;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack radon_withSigningContext(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return radon_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    @Unique
    private CommandSourceStack radon_copySuppliers(CommandSourceStack stack) {
        CommandSourceStackMixin stackMixin = (CommandSourceStackMixin)(Object)stack;
        stackMixin.radon_textNameSupplier = this.radon_textNameSupplier;
        stackMixin.radon_displayNameSupplier = this.radon_displayNameSupplier;
        return stack;
    }
}
