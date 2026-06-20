package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow int lastHurtByMobTimestamp;
    @Shadow Map<MobEffect, MobEffectInstance> activeEffects;
    @Shadow Brain<?> brain;
    @Shadow abstract Brain<?> makeBrain(Dynamic<?> dynamic);
    @Shadow abstract void setPosToBed(BlockPos pos);

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Health" -> nbt.putFloat("Health", entity.getHealth());
            case "HurtTime" -> nbt.putShort("HurtTime", (short) entity.hurtTime);
            case "HurtByTimestamp" -> nbt.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
            case "DeathTime" -> nbt.putShort("DeathTime", (short) entity.deathTime);
            case "AbsorptionAmount" -> nbt.putFloat("AbsorptionAmount", entity.getAbsorptionAmount());
            case "Attributes" -> nbt.put("Attributes", entity.getAttributes().save());
            case "ActiveEffects" -> {
                if (!this.activeEffects.isEmpty()) {
                    ListTag nbtList = new ListTag();
                    Iterator var3 = this.activeEffects.values().iterator();

                    while (var3.hasNext()) {
                        MobEffectInstance statusEffectInstance = (MobEffectInstance) var3.next();
                        nbtList.add(statusEffectInstance.save());
                    }

                    nbt.put("ActiveEffects", nbtList);
                }
            }
            case "FallFlying" -> nbt.putBoolean("FallFlying", entity.isFallFlying());
            case "SleepingX", "SleepingY", "SleepingZ" -> entity.getSleepingPos().ifPresent((pos) -> {
                nbt.putInt("SleepingX", pos.getX());
                nbt.putInt("SleepingY", pos.getY());
                nbt.putInt("SleepingZ", pos.getZ());
            });
            case "brain" -> {
                DataResult<Tag> dataResult = this.brain.serializeStart(NbtOps.INSTANCE);
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
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "AbsorptionAmount" -> entity.setAbsorptionAmount(nbt.getFloat("AbsorptionAmount"));
            case "Attributes" -> {
                if (nbt.contains("Attributes", 9) && this.level != null && !this.level.isClientSide)
                    entity.getAttributes().load(nbt.getList("Attributes", 10));
            }
            case "ActiveEffects" -> {
                if (nbt.contains("ActiveEffects", 9)) {
                    ListTag nbtList = nbt.getList("ActiveEffects", 10);
                    for (int i = 0; i < nbtList.size(); ++i) {
                        CompoundTag nbtCompound = nbtList.getCompound(i);
                        MobEffectInstance statusEffectInstance = MobEffectInstance.load(nbtCompound);
                        if (statusEffectInstance != null) {
                            this.activeEffects.put(statusEffectInstance.getEffect().value(), statusEffectInstance);
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
            case "HurtByTimestamp" -> this.lastHurtByMobTimestamp = nbt.getInt("HurtByTimestamp");
            case "Team" -> {
                if (nbt.contains("Team", 8)) {
                    String string = nbt.getString("Team");
                    PlayerTeam team = entity.level().getScoreboard().getPlayerTeam(string);
                    boolean bl = team != null && entity.level().getScoreboard().addPlayerToTeam(entity.getStringUUID(), team);
                    if (!bl) {
                        LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", string);
                    }
                }
            }
            case "FallFlying" -> {
                if (nbt.getBoolean("FallFlying"))
                    this.setSharedFlag(7, true);
            }
            case "Brain" -> {
                if (nbt.contains("Brain", 10))
                    this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Brain")));
            }
            case "SleepingX", "SleepingY", "SleepingZ" -> {
                Optional<BlockPos> currentPos = entity.getSleepingPos();
                if (currentPos.isPresent()) {
                    int i = nbt.contains("SleepingX") ? nbt.getInt("SleepingX") : currentPos.get().getX();
                    int j = nbt.contains("SleepingY") ? nbt.getInt("SleepingY") : currentPos.get().getY();
                    int k = nbt.contains("SleepingZ") ? nbt.getInt("SleepingZ") : currentPos.get().getZ();
                    BlockPos blockPos = new BlockPos(i, j, k);
                    entity.setSleepingPos(blockPos);
                    this.entityData.set(DATA_POSE, Pose.SLEEPING);
                    if (!this.firstTick) {
                        this.setPosToBed(blockPos);
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
