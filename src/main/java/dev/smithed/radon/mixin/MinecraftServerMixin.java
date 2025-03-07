package dev.smithed.radon.mixin;

import com.mojang.datafixers.DataFixer;
import dev.smithed.radon.mixin_interface.IMinecraftServerExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.registry.Registries;
import net.minecraft.resource.*;
import net.minecraft.server.*;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.ApiServices;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
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
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements CommandOutput, AutoCloseable, IMinecraftServerExtender {

    public MinecraftServerMixin(String string) {
        super(string);
    }

    @Shadow
    private MinecraftServer.ResourceManagerHolder resourceManagerHolder;
    @Shadow
    @Final
    protected SaveProperties saveProperties;

    private final Map<String, Set<String>> entityTypes = new HashMap<>();

    /**
     * @author ImCoolYeah105
     * Injects into constructor to build entityTypes map on load
     */
    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/server/SaveLoader;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/util/ApiServices;Lnet/minecraft/server/WorldGenerationProgressListenerFactory;)V",
            at = @At("TAIL")
    )
    private void radon_init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo cr) {
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
        Registries.ENTITY_TYPE.getTags().forEach(tag -> {
            final Set<String> entries = new HashSet<>();
            tag.forEach(item -> entries.add(NBTUtils.translationToTypeName(item.value().toString())));
            this.entityTypes.put(tag.getTag().id().toString(), entries);
        });
    }


    public Set<String> getEntityTagEntries(String tag) {
        return this.entityTypes.get(tag);
    }

}
