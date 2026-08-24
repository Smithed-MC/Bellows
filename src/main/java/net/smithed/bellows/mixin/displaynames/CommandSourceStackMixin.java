package net.smithed.bellows.mixin.displaynames;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.smithed.bellows.mixin_interface.displaynames.EntityExtender;
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
    private Supplier<String> bellows_textNameSupplier = null;
    @Unique
    private Supplier<Component> bellows_displayNameSupplier = null;

    /**
     * Modifies withEntity to use name suppliers instead of computing entity name every time context changes.
     * Cancels original method.
     * @author ICY105
     * @param entity - (from vanilla) entity being swapped to
     * @param cir - callback info
     */
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
            bellows_setSuppliers(newStack, extender);
            cir.setReturnValue(newStack);
        }
    }

    /**
     * Sets the name suppliers the supplied entity.
     * @param stack - new stack
     * @param entity - entity being swapped to
     */
    @Unique
    private void bellows_setSuppliers(CommandSourceStack stack, EntityExtender entity) {
        CommandSourceStackMixin stackMixin = (CommandSourceStackMixin)(Object)stack;
        stackMixin.bellows_textNameSupplier = entity.bellows_getPlainTextNameSupplier();
        stackMixin.bellows_displayNameSupplier = entity.bellows_getDisplayNameSupplier();
    }

    /**
     * Uses name supplier to set name if needed when get name is called.
     * @param cir - callback info
     */
    @Inject(method = "getTextName()Ljava/lang/String;", at = @At("HEAD"))
    public void bellows_getTextName(CallbackInfoReturnable<Component> cir) {
        if(textName == null) {
            textName = bellows_textNameSupplier.get();
        }
    }

    /**
     * Uses name supplier to set name if needed when get name is called.
     * @param cir - callback info
     */
    @Inject(method = "getDisplayName()Lnet/minecraft/network/chat/Component;", at = @At("HEAD"))
    public void bellows_getDisplayName(CallbackInfoReturnable<Component> cir) {
        if(displayName == null) {
            displayName = bellows_displayNameSupplier.get();
        }
    }

    /**
     * Makes command source context mutation copy name suppliers to new command source stack.
     * @param source - (from vanilla)
     * @param position - (from vanilla)
     * @param rotation - (from vanilla)
     * @param level - (from vanilla)
     * @param permissions - (from vanilla)
     * @param textName - (from vanilla)
     * @param displayName - (from vanilla)
     * @param server - (from vanilla)
     * @param entity - (from vanilla)
     * @param silent - (from vanilla)
     * @param resultCallback - (from vanilla)
     * @param anchor - (from vanilla)
     * @param signingContext - (from vanilla)
     * @param chatMessageChainer - (from vanilla)
     * @param original - (from vanilla)
     * @return CommandSourceStack - (from vanilla) new command source stack
     */
    @WrapOperation(
            method = "withSource(Lnet/minecraft/commands/CommandSource;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withSource(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withPosition(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withPosition(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withRotation(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withRotation(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withCallback(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withSuppressedOutput()Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withSuppressedOutput(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withPermission(Lnet/minecraft/server/permissions/PermissionSet;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withPermission(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withAnchor(Lnet/minecraft/commands/arguments/EntityAnchorArgument$Anchor;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withAnchor(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withLevel(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withLevel(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * @see CommandSourceStackMixin#bellows_withSource
     */
    @WrapOperation(
            method = "withSigningContext(Lnet/minecraft/commands/CommandSigningContext;Lnet/minecraft/util/TaskChainer;)Lnet/minecraft/commands/CommandSourceStack;",
            at = @At(value = "NEW", target = "Lnet/minecraft/commands/CommandSourceStack;")
    )
    public CommandSourceStack bellows_withSigningContext(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, boolean silent, CommandResultCallback resultCallback, EntityAnchorArgument.Anchor anchor, CommandSigningContext signingContext, TaskChainer chatMessageChainer, Operation<CommandSourceStack> original) {
        return bellows_copySuppliers(original.call(source, position, rotation, level, permissions, textName, displayName, server, entity, silent, resultCallback, anchor, signingContext, chatMessageChainer));
    }

    /**
     * Copies name suppliers to new CommandSourceStack instance.
     * @param stack - new CommandSourceStack
     * @return CommandSourceStack - new CommandSourceStack instance
     */
    @Unique
    private CommandSourceStack bellows_copySuppliers(CommandSourceStack stack) {
        CommandSourceStackMixin stackMixin = (CommandSourceStackMixin)(Object)stack;
        stackMixin.bellows_textNameSupplier = this.bellows_textNameSupplier;
        stackMixin.bellows_displayNameSupplier = this.bellows_displayNameSupplier;
        return stack;
    }
}
