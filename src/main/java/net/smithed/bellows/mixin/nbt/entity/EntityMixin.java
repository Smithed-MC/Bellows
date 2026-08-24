package net.smithed.bellows.mixin.nbt.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;
import net.smithed.bellows.mixin_interface.nbt.FilteredNbtAccessExtender;
import net.smithed.bellows.mixin_interface.selector.EntityLookupExtender;
import net.smithed.bellows.mixin_interface.selector.ServerLevelExtender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtender, FilteredNbtAccessExtender {

    @Shadow @Final
    protected static EntityDataAccessor<@NotNull Pose> DATA_POSE;
    @Shadow @Final
    private static Codec<List<String>> TAG_LIST_CODEC;
    @Shadow @Final
    protected SynchedEntityData entityData;
    @Shadow @Final
    private Set<String> tags;
    @Shadow
    private Level level;
    @Shadow
    private boolean hasVisualFire;
    @Shadow
    protected boolean firstTick;
    @Shadow
    private CustomData customData;

    @Shadow
    protected abstract void reapplyPosition();
    @Shadow
    protected abstract void setRot(float yaw, float pitch);
    @Shadow
    protected abstract boolean repositionEntityAfterLoad();
    @Shadow
    protected abstract void setSharedFlag(int index, boolean value);

    @WrapOperation(method = "addTag", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    public boolean addTag(Set instance, Object e, Operation<Boolean> original) {
        if (original.call(instance, e)) {
            if (this.level instanceof ServerLevelExtender world && world.bellows_getEntityIndex() instanceof EntityLookupExtender<?> index && e instanceof String tag) {
                index.bellows_addEntityToTagMap(tag, (Entity) (Object) this);
            }
            return true;
        }

        return false;
    }


    @WrapOperation(method = "removeTag", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    public boolean removeTag(Set instance, Object e, Operation<Boolean> original) {
        if (original.call(instance, e)) {
            if (this.level instanceof ServerLevelExtender world && world.bellows_getEntityIndex() instanceof EntityLookupExtender<?> index && e instanceof String tag) {
                index.bellows_removeEntityFromTagMap(tag, (Entity) (Object) this);
            }
            return true;
        }
        return false;
    }

    /**
     * remove entity from tag cache completely when tags are cleared
     */
    @Inject(method = "load(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void bellows_load(CallbackInfo ci) {
        if (this.level instanceof ServerLevelExtender world && world.bellows_getEntityIndex() instanceof EntityLookupExtender<?> index) {
            this.tags.forEach(tag -> index.bellows_removeEntityFromTagMap(tag, (Entity) (Object) this));
        }
    }

    /**
     * add entity to tag cache when tags are added via NBT data
     */
    @Inject(method = "load(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void bellows_load(ValueInput nbt, CallbackInfo ci) {
        if (nbt.contains("Tags") && this.level instanceof ServerLevelExtender world && world.bellows_getEntityIndex() != null) {
            Optional<List<String>> tagList = nbt.read("Tags", TAG_LIST_CODEC);
            if (tagList.isPresent()) {
                int max_size = Math.min(tagList.get().size(), 1024);
                for (int i = 0; i < max_size; ++i) {
                    world.bellows_getEntityIndex().bellows_addEntityToTagMap(tagList.get().get(i), (Entity) (Object) this);
                }
            }
        }
    }

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_saveWithoutIdFiltered(ValueOutput output, String path) {
        String topLevelNbt = path.split("[.{\\[]",1)[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos" -> {
                    if (entity.getVehicle() != null) {
                        output.store("Pos", Vec3.CODEC, new Vec3(entity.getVehicle().getX(), entity.getY(), entity.getVehicle().getZ()));
                    } else {
                        output.store("Pos", Vec3.CODEC, entity.position());
                    }
                }
                case "Motion" -> output.store("Motion", Vec3.CODEC, entity.getDeltaMovement());
                case "Rotation" -> output.store("Rotation", Vec2.CODEC, new Vec2(entity.getYRot(), entity.getXRot()));
                case "fall_distance" -> output.putDouble("fall_distance", entity.fallDistance);
                case "Fire" -> output.putShort("Fire", (short) entity.getRemainingFireTicks());
                case "Air" -> output.putShort("Air", (short) entity.getAirSupply());
                case "OnGround" -> output.putBoolean("OnGround", entity.onGround());
                case "Invulnerable" -> output.putBoolean("Invulnerable", entity.isInvulnerable());
                case "PortalCooldown" -> output.putInt("PortalCooldown", entity.getPortalCooldown());
                case "UUID" -> output.store("UUID", UUIDUtil.CODEC, entity.getUUID());
                case "CustomName" ->
                        output.storeNullable("CustomName", ComponentSerialization.CODEC, entity.getCustomName());
                case "CustomNameVisible" -> {
                    if (entity.isCustomNameVisible()) {
                        output.putBoolean("CustomNameVisible", entity.isCustomNameVisible());
                    }
                }
                case "Silent" -> {
                    if (entity.isSilent()) {
                        output.putBoolean("Silent", entity.isSilent());
                    }
                }
                case "NoGravity" -> {
                    if (entity.isNoGravity()) {
                        output.putBoolean("NoGravity", entity.isNoGravity());
                    }
                }
                case "Glowing" -> {
                    if (entity.hasGlowingTag()) {
                        output.putBoolean("Glowing", true);
                    }
                }
                case "TicksFrozen" -> {
                    int ticksFrozen = entity.getTicksFrozen();
                    if (ticksFrozen > 0) {
                        output.putInt("TicksFrozen", entity.getTicksFrozen());
                    }
                }
                case "HasVisualFire" -> {
                    if (this.hasVisualFire) {
                        output.putBoolean("HasVisualFire", this.hasVisualFire);
                    }
                }
                case "Tags" -> {
                    if (!this.tags.isEmpty()) {
                        output.store("Tags", TAG_LIST_CODEC, List.copyOf(this.tags));
                    }
                }
                case "data" -> {
                    if (!this.customData.isEmpty()) {
                        if(output instanceof TagValueOutput tagValueOutput) {
                            tagValueOutput.buildResult().put("data", this.customData.copyTag());
                        } else {
                            output.store("data", CustomData.CODEC, this.customData);
                        }
                    }
                }
                case "Passengers" -> {
                    if (entity.isVehicle()) {
                        ValueOutput.ValueOutputList passengersList = output.childrenList("Passengers");

                        for (Entity passenger : entity.getPassengers()) {
                            ValueOutput passengerOutput = passengersList.addChild();
                            if (!passenger.saveAsPassenger(passengerOutput)) {
                                passengersList.discardLast();
                            }
                        }

                        if (passengersList.isEmpty()) {
                            output.discard("Passengers");
                        }
                    }
                }
                default -> {
                    return this.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt);
                }
            }
            return true;
        } catch (Throwable var9) {
            CrashReport crashReport = CrashReport.forThrowable(var9, "Saving entity NBT");
            CrashReportCategory crashReportSection = crashReport.addCategory("Entity being saved");
            entity.fillCrashReportCategory(crashReportSection);
            throw new ReportedException(crashReport);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_loadFiltered(ValueInput input, String path) {
        String topLevelNbt = path.split("[\\[.{]",1)[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos" -> {
                    Vec3 pos = input.read("Pos", Vec3.CODEC).orElse(Vec3.ZERO);
                    entity.setPosRaw(Mth.clamp(pos.x, -3.0000512E7F, 3.0000512E7F), Mth.clamp(pos.y, -2.0E7F, 2.0E7F), Mth.clamp(pos.z, -3.0000512E7F, 3.0000512E7F));
                    entity.setOldPosAndRot();
                    this.reapplyPosition();
                }
                case "Motion" -> {
                    Vec3 motion = input.read("Motion", Vec3.CODEC).orElse(Vec3.ZERO);
                    entity.setDeltaMovement(Math.abs(motion.x) > (double) 10.0F ? (double) 0.0F : motion.x, Math.abs(motion.y) > (double) 10.0F ? (double) 0.0F : motion.y, Math.abs(motion.z) > (double) 10.0F ? (double) 0.0F : motion.z);
                }
                case "Rotation" -> {
                    Vec2 rotation = input.read("Rotation", Vec2.CODEC).orElse(Vec2.ZERO);
                    entity.setYRot(rotation.x);
                    entity.setXRot(rotation.y);
                    entity.setOldPosAndRot();
                    entity.setYHeadRot(entity.getYRot());
                    entity.setYBodyRot(entity.getYRot());
                    this.setRot(entity.getYRot(), entity.getXRot());
                }
                case "fall_distance" -> entity.fallDistance = input.getDoubleOr("fall_distance", 0.0F);
                case "Fire" -> entity.setRemainingFireTicks(input.getShortOr("Fire", (short) 0));
                case "Air" -> entity.setAirSupply(input.getIntOr("Air", entity.getMaxAirSupply()));
                case "OnGround" -> entity.setOnGround(input.getBooleanOr("OnGround", false));
                case "Invulnerable" -> entity.setInvulnerable(input.getBooleanOr("Invulnerable", false));
                case "PortalCooldown" -> entity.setPortalCooldown(input.getIntOr("PortalCooldown", 0));
                case "UUID" -> input.read("UUID", UUIDUtil.CODEC).ifPresent(entity::setUUID);
                case "CustomName" ->
                        entity.setCustomName(input.read("CustomName", ComponentSerialization.CODEC).orElse(null));
                case "CustomNameVisible" -> entity.setCustomNameVisible(input.getBooleanOr("CustomNameVisible", false));
                case "Silent" -> entity.setSilent(input.getBooleanOr("Silent", false));
                case "NoGravity" -> entity.setNoGravity(input.getBooleanOr("NoGravity", false));
                case "Glowing" -> entity.setGlowingTag(input.getBooleanOr("Glowing", false));
                case "TicksFrozen" -> entity.setTicksFrozen(input.getIntOr("TicksFrozen", 0));
                case "HasVisualFire" -> this.hasVisualFire = input.getBooleanOr("HasVisualFire", false);
                case "data" -> this.customData = input.read("data", CustomData.CODEC).orElse(CustomData.EMPTY);
                case "Tags" -> {
                    if (this.level instanceof ServerLevelExtender world && world.bellows_getEntityIndex() instanceof EntityLookupExtender<?> index) {
                        this.tags.forEach(tag -> index.bellows_removeEntityFromTagMap(tag, entity));
                        this.tags.clear();

                        Optional<List<String>> tagList = input.read("Tags", TAG_LIST_CODEC);
                        if (tagList.isPresent()) {
                            int max_size = Math.min(tagList.get().size(), 1024);
                            for (int i = 0; i < max_size; ++i) {
                                index.bellows_addEntityToTagMap(tagList.get().get(i), entity);
                            }
                            this.tags.addAll(tagList.get());
                        }
                    }
                }
                default -> {
                    if (this.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
                        if (this.repositionEntityAfterLoad()) {
                            this.reapplyPosition();
                        }
//                      if (topLevelNbt.equals("ArmorItems") || topLevelNbt.equals("HandItems")) {
//                          IntegrationRouter.triggerEquipmentUpdate(this);
//                      }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (Throwable var17) {
            CrashReport crashReport = CrashReport.forThrowable(var17, "Loading entity NBT");
            CrashReportCategory crashReportSection = crashReport.addCategory("Entity being loaded");
            entity.fillCrashReportCategory(crashReportSection);
            throw new ReportedException(crashReport);
        }
    }
}
