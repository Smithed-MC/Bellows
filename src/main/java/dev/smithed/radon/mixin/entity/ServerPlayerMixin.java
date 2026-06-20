package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.Set;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final ServerRecipeBook recipeBook;
    @Shadow Vec3 enteredNetherPosition;
    @Shadow boolean seenCredits;
    @Shadow BlockPos respawnPosition;
    @Shadow boolean respawnForced;
    @Shadow float respawnAngle;
    @Shadow ResourceKey<Level> respawnDimension;
    @Shadow WardenSpawnTracker wardenSpawnTracker;
    @Shadow boolean spawnExtraParticlesOnFall;
    @Nullable @Shadow BlockPos raidOmenPosition;
    @Shadow @Final Set<ThrownEnderpearl> enderPearls;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        ServerPlayer entity = ((ServerPlayer) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "warden_spawn_tracker":
                DataResult<Tag> var10000 = WardenSpawnTracker.CODEC.encodeStart(NbtOps.INSTANCE, this.wardenSpawnTracker);
                Logger var10001 = LOGGER;
                Objects.requireNonNull(var10001);
                var10000.resultOrPartial(var10001::error).ifPresent((encoded) -> {
                    nbt.put("warden_spawn_tracker", encoded);
                });
                break;
            case "playerGameType":
                nbt.putInt("playerGameType", entity.gameMode.getGameModeForPlayer().getId());
                break;
            case "previousPlayerGameType":
                GameType gameMode = entity.gameMode.getPreviousGameModeForPlayer();
                if (gameMode != null)
                    nbt.putInt("previousPlayerGameType", gameMode.getId());
                break;
            case "seenCredits":
                nbt.putBoolean("seenCredits", this.seenCredits);
                break;
            case "enteredNetherPosition":
                if (this.enteredNetherPosition != null) {
                    CompoundTag nbtCompound = new CompoundTag();
                    nbtCompound.putDouble("x", this.enteredNetherPosition.x);
                    nbtCompound.putDouble("y", this.enteredNetherPosition.y);
                    nbtCompound.putDouble("z", this.enteredNetherPosition.z);
                    nbt.put("enteredNetherPosition", nbtCompound);
                }
                break;
            case "RootVehicle":
                Entity vehicleEntity = entity.getRootVehicle();
                Entity entity2 = entity.getVehicle();
                if (entity2 != null && vehicleEntity != entity && entity.hasExactlyOnePlayerPassenger()) {
                    CompoundTag nbtCompound2 = new CompoundTag();
                    CompoundTag nbtCompound3 = new CompoundTag();
                    entity.save(nbtCompound3);
                    nbtCompound2.putUUID("Attach", entity2.getUUID());
                    nbtCompound2.put("Entity", nbtCompound3);
                    nbt.put("RootVehicle", nbtCompound2);
                }
                break;
            case "recipeBook":
                nbt.put("recipeBook", this.recipeBook.toNbt());
                break;
            case "Dimension":
                nbt.putString("Dimension", entity.level().dimension().location().toString());
                break;
            case "SpawnX":
                if (this.respawnPosition != null)
                    nbt.putInt("SpawnX", this.respawnPosition.getX());
                break;
            case "SpawnY":
                if (this.respawnPosition != null)
                    nbt.putInt("SpawnY", this.respawnPosition.getY());
            case "SpawnZ":
                if (this.respawnPosition != null)
                    nbt.putInt("SpawnZ", this.respawnPosition.getZ());
                break;
            case "SpawnForced":
                if (this.respawnPosition != null)
                    nbt.putBoolean("SpawnForced", this.respawnForced);
                break;
            case "SpawnAngle":
                if (this.respawnPosition != null)
                    nbt.putFloat("SpawnAngle", this.respawnAngle);
                break;
            case "SpawnDimension":
                if (this.respawnPosition != null) {
                    DataResult<Tag> var10002 = ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.respawnDimension.location());
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
                if (this.raidOmenPosition != null) {
                    var10000 = BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.raidOmenPosition);
                    var10001 = LOGGER;
                    Objects.requireNonNull(var10001);
                    var10000.resultOrPartial(var10001::error).ifPresent((encoded) -> {
                        nbt.put("raid_omen_position", encoded);
                    });
                }
                break;
            case "ender_pearls":
                if (!this.enderPearls.isEmpty()) {
                    ListTag nbtList = new ListTag();

                    for (ThrownEnderpearl enderPearlEntity : this.enderPearls) {
                        if (enderPearlEntity.isRemoved()) {
                            LOGGER.warn("Trying to save removed ender pearl, skipping");
                        } else {
                            CompoundTag nbtCompound = new CompoundTag();
                            enderPearlEntity.save(nbtCompound);
                            DataResult<Tag> var10004 = ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, enderPearlEntity.level().dimension().location());
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
        return true;
    }

}
