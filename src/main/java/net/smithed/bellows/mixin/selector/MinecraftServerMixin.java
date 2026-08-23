package net.smithed.bellows.mixin.selector;

import com.mojang.datafixers.DataFixer;
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
import net.smithed.bellows.mixin_interface.selector.MinecraftServerExtender;
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
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<@NotNull TickTask> implements CommandSource, AutoCloseable, MinecraftServerExtender {

    public MinecraftServerMixin(String name, boolean propagatesCrashes) {
        super(name, propagatesCrashes);
    }

    @Unique
    private final Map<String, Set<String>> bellows_entityTypes = new HashMap<>();

    /**
     * Constructs entity type tag cache on init.
     * @author ImCoolYeah105
     * @param serverThread - (from vanilla)
     * @param storageSource - (from vanilla)
     * @param packRepository - (from vanilla)
     * @param worldStem - (from vanilla)
     * @param gameRules - (from vanilla)
     * @param proxy - (from vanilla)
     * @param fixerUpper - (from vanilla)
     * @param services - (from vanilla)
     * @param levelLoadListener - (from vanilla)
     * @param propagatesCrashes - (from vanilla)
     * @param notificationManager - (from vanilla)
     * @param ci - callback info
     */
    @Inject(
        method = "<init>(Ljava/lang/Thread;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Ljava/util/Optional;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/server/Services;Lnet/minecraft/server/level/progress/LevelLoadListener;ZLnet/minecraft/server/notifications/NotificationManager;)V",
        at = @At("TAIL")
    )
    private void bellows_init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, NotificationManager notificationManager, CallbackInfo ci) {
        this.bellows_constructEntityTypes();
    }

    /**
     * Constructs entity type tag cache on reload.
     * @author ImCoolYeah105
     * @param ci - callback info
     */
    @Inject(method = "reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"))
    public void bellows_reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> ci) {
        if (ci.getReturnValue().isDone()) {
            this.bellows_constructEntityTypes();
        } else {
            ci.getReturnValue().thenAcceptAsync(resourceManagerHolder -> this.bellows_constructEntityTypes());
        }
    }

    /**
     * Rebuilds entity type tag cache.
     */
    @Unique
    private void bellows_constructEntityTypes() {
        this.bellows_entityTypes.clear();
        BuiltInRegistries.ENTITY_TYPE.listTags().forEach(tag -> {
            final Set<String> entries = new HashSet<>();
            tag.forEach(item -> entries.add(item.getRegisteredName()));
            this.bellows_entityTypes.put(tag.key().location().toString(), entries);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> bellows_getEntityTagEntries(String tag) {
        return this.bellows_entityTypes.get(tag);
    }
}
