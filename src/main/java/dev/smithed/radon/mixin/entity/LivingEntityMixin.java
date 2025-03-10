package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow @Final static Logger LOGGER;
    @Shadow int lastAttackedTime;
    @Shadow Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
    @Shadow Brain<?> brain;
    @Shadow abstract Brain<?> deserializeBrain(Dynamic<?> dynamic);
    @Shadow abstract void setPositionInBed(BlockPos pos);

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Health" -> nbt.putFloat("Health", entity.getHealth());
            case "HurtTime" -> nbt.putShort("HurtTime", (short) entity.hurtTime);
            case "HurtByTimestamp" -> nbt.putInt("HurtByTimestamp", this.lastAttackedTime);
            case "DeathTime" -> nbt.putShort("DeathTime", (short) entity.deathTime);
            case "AbsorptionAmount" -> nbt.putFloat("AbsorptionAmount", entity.getAbsorptionAmount());
            case "Attributes" -> nbt.put("Attributes", entity.getAttributes().toNbt());
            case "ActiveEffects" -> {
                if (!this.activeStatusEffects.isEmpty()) {
                    NbtList nbtList = new NbtList();
                    Iterator var3 = this.activeStatusEffects.values().iterator();

                    while (var3.hasNext()) {
                        StatusEffectInstance statusEffectInstance = (StatusEffectInstance) var3.next();
                        nbtList.add(statusEffectInstance.writeNbt());
                    }

                    nbt.put("ActiveEffects", nbtList);
                }
            }
            case "FallFlying" -> nbt.putBoolean("FallFlying", entity.isGliding());
            case "SleepingX", "SleepingY", "SleepingZ" -> entity.getSleepingPosition().ifPresent((pos) -> {
                nbt.putInt("SleepingX", pos.getX());
                nbt.putInt("SleepingY", pos.getY());
                nbt.putInt("SleepingZ", pos.getZ());
            });
            case "brain" -> {
                DataResult<NbtElement> dataResult = this.brain.encode(NbtOps.INSTANCE);
                Logger var10001 = LOGGER;
                java.util.Objects.requireNonNull(var10001);
                dataResult.resultOrPartial(var10001::error).ifPresent((brain) -> {
                    nbt.put("Brain", brain);
                });
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "AbsorptionAmount" -> entity.setAbsorptionAmount(nbt.getFloat("AbsorptionAmount"));
            case "Attributes" -> {
                if (nbt.contains("Attributes", 9) && this.world != null && !this.world.isClient)
                    entity.getAttributes().readNbt(nbt.getList("Attributes", 10));
            }
            case "ActiveEffects" -> {
                if (nbt.contains("ActiveEffects", 9)) {
                    NbtList nbtList = nbt.getList("ActiveEffects", 10);
                    for (int i = 0; i < nbtList.size(); ++i) {
                        NbtCompound nbtCompound = nbtList.getCompound(i);
                        StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                        if (statusEffectInstance != null) {
                            this.activeStatusEffects.put(statusEffectInstance.getEffectType().value(), statusEffectInstance);
                        }
                    }
                }
            }
            case "Health" -> {
                if (nbt.contains("Health", 99))
                    entity.setHealth(nbt.getFloat("Health"));
            }
            case "HurtTime" -> {
                entity.hurtTime = nbt.getShort("HurtTime");
            }
            case "DeathTime" -> entity.deathTime = nbt.getShort("DeathTime");
            case "HurtByTimestamp" -> this.lastAttackedTime = nbt.getInt("HurtByTimestamp");
            case "Team" -> {
                if (nbt.contains("Team", 8)) {
                    String string = nbt.getString("Team");
                    Team team = entity.getWorld().getScoreboard().getTeam(string);
                    boolean bl = team != null && entity.getWorld().getScoreboard().addScoreHolderToTeam(entity.getUuidAsString(), team);
                    if (!bl) {
                        LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", string);
                    }
                }
            }
            case "FallFlying" -> {
                if (nbt.getBoolean("FallFlying"))
                    this.setFlag(7, true);
            }
            case "Brain" -> {
                if (nbt.contains("Brain", 10))
                    this.brain = this.deserializeBrain(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Brain")));
            }
            case "SleepingX", "SleepingY", "SleepingZ" -> {
                Optional<BlockPos> currentPos = entity.getSleepingPosition();
                if (currentPos.isPresent()) {
                    int i = nbt.contains("SleepingX") ? nbt.getInt("SleepingX") : currentPos.get().getX();
                    int j = nbt.contains("SleepingY") ? nbt.getInt("SleepingY") : currentPos.get().getY();
                    int k = nbt.contains("SleepingZ") ? nbt.getInt("SleepingZ") : currentPos.get().getZ();
                    BlockPos blockPos = new BlockPos(i, j, k);
                    entity.setSleepingPosition(blockPos);
                    this.dataTracker.set(POSE, EntityPose.SLEEPING);
                    if (!this.firstUpdate) {
                        this.setPositionInBed(blockPos);
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
