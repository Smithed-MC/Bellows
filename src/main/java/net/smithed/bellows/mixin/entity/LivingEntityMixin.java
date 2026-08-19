package net.smithed.bellows.mixin.entity;

import net.smithed.bellows.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.waypoints.Waypoint;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;
    @Shadow @Final protected EntityEquipment equipment;
    @Shadow protected Brain<?> brain;
    @Shadow private Waypoint.Icon locatorBarIcon;
    @Shadow private boolean effectsDirty = true;
    @Shadow private int lastHurtByMobTimestamp;
    @Shadow protected int lastHurtByPlayerMemoryTime;
    @Shadow private int currentImpulseContextResetGraceTime;
    @Shadow protected abstract void setPosToBed(BlockPos bedPosition);
    @Shadow protected @Nullable EntityReference<@NotNull Player> lastHurtByPlayer;
    @Shadow private @Nullable EntityReference<@NotNull LivingEntity> lastHurtByMob;
    @Shadow abstract protected void internalSetAbsorptionAmount(final float absorptionAmount);
    @Shadow abstract protected Brain<? extends @NotNull LivingEntity> makeBrain(final Brain.Packed packedBrain);

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        LivingEntity entity = ((LivingEntity)(Object)this);

        switch (topLevelNbt) {
            case "Health" -> output.putFloat("Health", entity.getHealth());
            case "HurtTime" -> output.putShort("HurtTime", (short)entity.hurtTime);
            case "DeathTime" -> output.putShort("DeathTime", (short)entity.deathTime);
            case "AbsorptionAmount" -> output.putFloat("AbsorptionAmount", entity.getAbsorptionAmount());
            case "current_impulse_context_reset_grace_time" -> output.putInt("current_impulse_context_reset_grace_time", this.currentImpulseContextResetGraceTime);
            case "current_explosion_impact_pos" -> output.storeNullable("current_explosion_impact_pos", Vec3.CODEC, entity.currentImpulseImpactPos);
            case "Attributes" -> output.store("attributes", net.minecraft.world.entity.ai.attributes.AttributeInstance.Packed.LIST_CODEC, entity.getAttributes().pack());
            case "ActiveEffects" -> {
                if (!this.activeEffects.isEmpty()) {
                    output.store("active_effects", MobEffectInstance.CODEC.listOf(), List.copyOf(this.activeEffects.values()));
                }
            }
            case "FallFlying" -> output.putBoolean("FallFlying", entity.isFallFlying());
            case "sleeping_pos" -> entity.getSleepingPos().ifPresent((sleepingPos) -> output.store("sleeping_pos", BlockPos.CODEC, sleepingPos));
            case "brain" -> output.store("Brain", Brain.Packed.CODEC, this.brain.pack());
            case "last_hurt_by_player", "last_hurt_by_player_memory_time" -> {
                if (this.lastHurtByPlayer != null) {
                    this.lastHurtByPlayer.store(output, "last_hurt_by_player");
                    output.putInt("last_hurt_by_player_memory_time", this.lastHurtByPlayerMemoryTime);
                }
            }
            case "last_hurt_by_mob", "ticks_since_last_hurt_by_mob" -> {
                if (this.lastHurtByMob != null) {
                    this.lastHurtByMob.store(output, "last_hurt_by_mob");
                    output.putInt("ticks_since_last_hurt_by_mob", entity.tickCount - entity.getLastHurtMobTimestamp());
                }
            }
            case "equipment" -> {
                if (!this.equipment.isEmpty()) {
                    output.store("equipment", EntityEquipment.CODEC, this.equipment);
                }
            }
            case "locator_bar_icon" -> {
                if (this.locatorBarIcon.hasData()) {
                    output.store("locator_bar_icon", Waypoint.Icon.CODEC, this.locatorBarIcon);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        LivingEntity entity = ((LivingEntity)(Object)this);

        switch (topLevelNbt) {
            case "AbsorptionAmount" -> this.internalSetAbsorptionAmount(input.getFloatOr("AbsorptionAmount", 0.0F));
            case "Attributes" -> {
                if (entity.level() != null && !entity.level().isClientSide()) {
                    Optional<List<AttributeInstance.Packed>> var10000 = input.read("attributes", net.minecraft.world.entity.ai.attributes.AttributeInstance.Packed.LIST_CODEC);
                    AttributeMap attributes = entity.getAttributes();
                    Objects.requireNonNull(attributes);
                    var10000.ifPresent(attributes::apply);
                }
            }
            case "ActiveEffects" -> {
                List<MobEffectInstance> effects = input.read("active_effects", MobEffectInstance.CODEC.listOf()).orElse(List.of());
                this.activeEffects.clear();

                for(MobEffectInstance effect : effects) {
                    this.activeEffects.put(effect.getEffect().value(), effect);
                    this.effectsDirty = true;
                }
            }
            case "Health" -> entity.setHealth(input.getFloatOr("Health", entity.getMaxHealth()));
            case "HurtTime" -> entity.hurtTime = input.getShortOr("HurtTime", (short)0);
            case "DeathTime" -> entity.deathTime = input.getShortOr("DeathTime", (short)0);
            case "Team" -> {
                input.getString("Team").ifPresent((teamName) -> {
                    Scoreboard scoreboard = entity.level().getScoreboard();
                    PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                    boolean success = team != null && scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
                    if (!success) {
                        LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", teamName);
                    }
                });
            }
            case "FallFlying" -> this.setSharedFlag(7, input.getBooleanOr("FallFlying", false));
            case "sleeping_pos" -> {
                input.read("sleeping_pos", BlockPos.CODEC).ifPresentOrElse((sleepingPos) -> {
                    entity.setSleepingPos(sleepingPos);
                    this.entityData.set(DATA_POSE, Pose.SLEEPING);
                    if (!this.firstTick) {
                        this.setPosToBed(sleepingPos);
                    }

                }, entity::clearSleepingPos);
            }
            case "Brain" -> input.read("Brain", Brain.Packed.CODEC).ifPresent((packedBrain) -> this.brain = this.makeBrain(packedBrain));
            case "last_hurt_by_player" -> this.lastHurtByPlayer = EntityReference.read(input, "last_hurt_by_player");
            case "last_hurt_by_player_memory_time" -> this.lastHurtByPlayerMemoryTime = input.getIntOr("last_hurt_by_player_memory_time", 0);
            case "last_hurt_by_mob" -> this.lastHurtByMob = EntityReference.read(input, "last_hurt_by_mob");
            case "ticks_since_last_hurt_by_mob" -> this.lastHurtByMobTimestamp = entity.tickCount - input.getIntOr("ticks_since_last_hurt_by_mob", 0);
            case "equipment" -> this.equipment.setAll(input.read("equipment", EntityEquipment.CODEC).orElseGet(EntityEquipment::new));
            case "locator_bar_icon" -> this.locatorBarIcon = input.read("locator_bar_icon", Waypoint.Icon.CODEC).orElseGet(Waypoint.Icon::new);
            case "current_impulse_context_reset_grace_time" -> this.currentImpulseContextResetGraceTime = input.getIntOr("current_impulse_context_reset_grace_time", 0);
            case "current_explosion_impact_pos" -> entity.currentImpulseImpactPos = (Vec3)input.read("current_explosion_impact_pos", Vec3.CODEC).orElse(null);
            default -> {
                return false;
            }
        }
        return true;
    }
}
