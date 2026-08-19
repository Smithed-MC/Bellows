package dev.smithed.radon.mixin;

import com.mojang.datafixers.DataFixer;
import dev.smithed.radon.mixin_interface.IMinecraftServerExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<@NotNull TickTask> implements CommandSource, AutoCloseable, IMinecraftServerExtender {

    public MinecraftServerMixin(String name, boolean propagatesCrashes) {
        super(name, propagatesCrashes);
    }

    @Unique
    private final Map<String, Set<String>> entityTypes = new HashMap<>();

    /**
     * @author ImCoolYeah105
     * Injects into constructor to build entityTypes map on load
     */
    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Ljava/util/Optional;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/server/Services;Lnet/minecraft/server/level/progress/LevelLoadListener;ZLnet/minecraft/server/notifications/NotificationManager;)V",
            at = @At("TAIL")
    )
    private void bellows_init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, NotificationManager notificationManager, CallbackInfo ci) {
        this.bellows_constructEntityTypes();
    }

    /**
     * @author ImCoolYeah105
     * Injects into resource reloading to rebuild entityTypes map
     */
    @Inject(method = "reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"))
    public void bellows_reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> ci) {
        if (ci.getReturnValue().isDone()) {
            this.bellows_constructEntityTypes();
        } else {
            ci.getReturnValue().thenAcceptAsync(resourceManagerHolder -> this.bellows_constructEntityTypes());
        }
    }

    @Unique
    private void bellows_constructEntityTypes() {
        this.entityTypes.clear();
        BuiltInRegistries.ENTITY_TYPE.listTags().forEach(tag -> {
            final Set<String> entries = new HashSet<>();
            tag.forEach(item -> entries.add(NBTUtils.translationToTypeName(item.value().toString())));
            this.entityTypes.put(tag.key().location().toString(), entries);
        });
    }

    @Override
    public Set<String> bellows_getEntityTagEntries(String tag) {
        return this.entityTypes.get(tag);
    }
}
