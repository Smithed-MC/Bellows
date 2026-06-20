package dev.smithed.radon.mixin;

import com.mojang.datafixers.DataFixer;
import dev.smithed.radon.mixin_interface.IMinecraftServerExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements CommandSource, AutoCloseable, IMinecraftServerExtender {

    public MinecraftServerMixin(String string) {
        super(string);
    }

    @Shadow
    private MinecraftServer.ReloadableResources resources;
    @Shadow
    @Final
    protected WorldData worldData;

    private final Map<String, Set<String>> entityTypes = new HashMap<>();

    /**
     * @author ImCoolYeah105
     * Injects into constructor to build entityTypes map on load
     */
    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/server/Services;Lnet/minecraft/server/level/progress/ChunkProgressListenerFactory;)V",
            at = @At("TAIL")
    )
    private void radon_init(Thread serverThread, LevelStorageSource.LevelStorageAccess session, PackRepository dataPackManager, WorldStem saveLoader, Proxy proxy, DataFixer dataFixer, Services apiServices, ChunkProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo cr) {
        this.constructEntityTypes();
    }

    /**
     * @author ImCoolYeah105
     * Injects into resource reloading to rebuild entityTypes map
     */
    @Inject(method = "reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"))
    public void radon_reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> ci) {
        if (ci.getReturnValue().isDone())
            this.constructEntityTypes();
        else
            ci.getReturnValue().thenAcceptAsync(resourceManagerHolder -> this.constructEntityTypes());
    }

    private void constructEntityTypes() {
        this.entityTypes.clear();
        BuiltInRegistries.ENTITY_TYPE.listTags().forEach(tag -> {
            final Set<String> entries = new HashSet<>();
            tag.forEach(item -> entries.add(NBTUtils.translationToTypeName(item.value().toString())));
            this.entityTypes.put(tag.key().location().toString(), entries);
        });
    }


    public Set<String> getEntityTagEntries(String tag) {
        return this.entityTypes.get(tag);
    }

}
