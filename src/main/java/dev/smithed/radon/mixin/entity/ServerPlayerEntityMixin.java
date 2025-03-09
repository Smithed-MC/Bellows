package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.Set;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ICustomNBTMixin {

    @Shadow @Final static Logger LOGGER;
    @Shadow @Final ServerRecipeBook recipeBook;
    @Shadow Vec3d enteredNetherPos;
    @Shadow boolean seenCredits;
    @Shadow BlockPos spawnPointPosition;
    @Shadow boolean spawnForced;
    @Shadow float spawnAngle;
    @Shadow RegistryKey<World> spawnPointDimension;
    @Shadow SculkShriekerWarningManager sculkShriekerWarningManager;
    @Shadow boolean spawnExtraParticlesOnFall;
    @Nullable @Shadow BlockPos startRaidPos;
    @Shadow @Final Set<EnderPearlEntity> enderPearls;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        ServerPlayerEntity entity = ((ServerPlayerEntity) (Object) this);
        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            switch (topLevelNbt) {
                case "warden_spawn_tracker":
                    DataResult<NbtElement> var10000 = SculkShriekerWarningManager.CODEC.encodeStart(NbtOps.INSTANCE, this.sculkShriekerWarningManager);
                    Logger var10001 = LOGGER;
                    Objects.requireNonNull(var10001);
                    var10000.resultOrPartial(var10001::error).ifPresent((encoded) -> {
                        nbt.put("warden_spawn_tracker", encoded);
                    });
                    break;
                case "playerGameType":
                    nbt.putInt("playerGameType", entity.interactionManager.getGameMode().getId());
                    break;
                case "previousPlayerGameType":
                    GameMode gameMode = entity.interactionManager.getPreviousGameMode();
                    if (gameMode != null)
                        nbt.putInt("previousPlayerGameType", gameMode.getId());
                    break;
                case "seenCredits":
                    nbt.putBoolean("seenCredits", this.seenCredits);
                    break;
                case "enteredNetherPosition":
                    if (this.enteredNetherPos != null) {
                        NbtCompound nbtCompound = new NbtCompound();
                        nbtCompound.putDouble("x", this.enteredNetherPos.x);
                        nbtCompound.putDouble("y", this.enteredNetherPos.y);
                        nbtCompound.putDouble("z", this.enteredNetherPos.z);
                        nbt.put("enteredNetherPosition", nbtCompound);
                    }
                    break;
                case "RootVehicle":
                    Entity vehicleEntity = entity.getRootVehicle();
                    Entity entity2 = entity.getVehicle();
                    if (entity2 != null && vehicleEntity != entity && entity.hasPlayerRider()) {
                        NbtCompound nbtCompound2 = new NbtCompound();
                        NbtCompound nbtCompound3 = new NbtCompound();
                        entity.saveNbt(nbtCompound3);
                        nbtCompound2.putUuid("Attach", entity2.getUuid());
                        nbtCompound2.put("Entity", nbtCompound3);
                        nbt.put("RootVehicle", nbtCompound2);
                    }
                    break;
                case "recipeBook":
                    nbt.put("recipeBook", this.recipeBook.toNbt());
                    break;
                case "Dimension":
                    nbt.putString("Dimension", entity.getWorld().getRegistryKey().getValue().toString());
                    break;
                case "SpawnX":
                    if (this.spawnPointPosition != null)
                        nbt.putInt("SpawnX", this.spawnPointPosition.getX());
                    break;
                case "SpawnY":
                    if (this.spawnPointPosition != null)
                        nbt.putInt("SpawnY", this.spawnPointPosition.getY());
                case "SpawnZ":
                    if (this.spawnPointPosition != null)
                        nbt.putInt("SpawnZ", this.spawnPointPosition.getZ());
                    break;
                case "SpawnForced":
                    if (this.spawnPointPosition != null)
                        nbt.putBoolean("SpawnForced", this.spawnForced);
                    break;
                case "SpawnAngle":
                    if (this.spawnPointPosition != null)
                        nbt.putFloat("SpawnAngle", this.spawnAngle);
                    break;
                case "SpawnDimension":
                    if (this.spawnPointPosition != null) {
                        DataResult<NbtElement> var10002 = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPointDimension.getValue());
                        Logger var10003 = Radon.LOGGER;
                        Objects.requireNonNull(var10003);
                        var10002.resultOrPartial(var10003::error).ifPresent((nbtElement) -> {
                            nbt.put("SpawnDimension", nbtElement);
                        });
                    }
                    break;
                case "spawn_extra_particles_on_fall":
                    nbt.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
                    break;
                case "raid_omen_position":
                    if (this.startRaidPos != null) {
                        var10000 = BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.startRaidPos);
                        var10001 = LOGGER;
                        Objects.requireNonNull(var10001);
                        var10000.resultOrPartial(var10001::error).ifPresent((encoded) -> {
                            nbt.put("raid_omen_position", encoded);
                        });
                    }
                    break;
                case "ender_pearls":
                    if (!this.enderPearls.isEmpty()) {
                        NbtList nbtList = new NbtList();

                        for (EnderPearlEntity enderPearlEntity : this.enderPearls) {
                            if (enderPearlEntity.isRemoved()) {
                                LOGGER.warn("Trying to save removed ender pearl, skipping");
                            } else {
                                NbtCompound nbtCompound = new NbtCompound();
                                enderPearlEntity.saveNbt(nbtCompound);
                                DataResult<NbtElement> var10004 = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, enderPearlEntity.getWorld().getRegistryKey().getValue());
                                Logger var10005 = LOGGER;
                                Objects.requireNonNull(var10005);
                                var10004.resultOrPartial(var10005::error).ifPresent((dimension) -> {
                                    nbtCompound.put("ender_pearl_dimension", dimension);
                                });
                                nbtList.add(nbtCompound);
                            }
                        }

                        nbt.put("ender_pearls", nbtList);
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

}
