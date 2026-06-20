package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEntityIndexExtender;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityMixin, ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final static EntityDataAccessor<Pose> DATA_POSE;
    @Shadow Level level;
    @Shadow SynchedEntityData entityData;
    @Shadow Entity vehicle;
    @Shadow int remainingFireTicks;
    @Shadow boolean onGround;
    @Shadow boolean invulnerable;
    @Shadow int portalCooldown;
    @Shadow boolean hasGlowingTag;
    @Shadow boolean hasVisualFire;
    @Shadow Set<String> tags;
    @Shadow UUID uuid;
    @Shadow float fallDistance;
    @Shadow String stringUUID;
    @Shadow boolean firstTick;
    @Shadow @Final static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;

    @Shadow abstract void reapplyPosition();
    @Shadow abstract void setRot(float yaw, float pitch);
    @Shadow abstract boolean repositionEntityAfterLoad();
    @Shadow abstract ListTag newDoubleList(double... values);
    @Shadow abstract ListTag newFloatList(float... values);
    @Shadow abstract void setSharedFlag(int index, boolean value);
    @Shadow protected abstract RegistryAccess registryAccess();

    /**
     * @author ImCoolYeah105
     * @reason 1 line overwrite
     * Add entity to tag cache when a tag is added
     */
    @Overwrite
    public boolean addTag(String tag) {
        if (this.tags.size() < 1024 && this.tags.add(tag)) {
            if (this.level instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
                index.addEntityToTagMap(tag, (Entity) (Object) this);
            return true;
        }
        return false;
    }

    /**
     * @author ImCoolYeah105
     * @reason 1 line overwrite
     * Remove entity from tag cache when a tag is removed
     */
    @Overwrite
    public boolean removeTag(String tag) {
        if (this.tags.remove(tag)) {
            if (this.level instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
                index.removeEntityFromTagMap(tag, (Entity) (Object) this);
            return true;
        }
        return false;
    }

    /**
     * remove entity from tag cache completely when tags are cleared
     */
    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void radon_load(CallbackInfo ci) {
        if (this.level instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
            this.tags.forEach(tag -> index.removeEntityFromTagMap(tag, (Entity) (Object) this));
    }

    /**
     * add entity to tag cache when tags are added via NBT data
     */
    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void radon_load(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("Tags", 9) && this.level instanceof IServerWorldExtender world && world.getEntityIndex().getEntity(uuid) != null && world.getEntityIndex() instanceof IEntityIndexExtender index) {
            ListTag nbtList4 = nbt.getList("Tags", 8);
            int i = Math.min(nbtList4.size(), 1024);

            for (int j = 0; j < i; ++j) {
                index.addEntityToTagMap(nbtList4.getString(j), (Entity) (Object) this);
            }
        }
    }

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public CompoundTag saveWithoutIdFiltered(CompoundTag nbt, String path) {
        String topLevelNbt = path.split("[\\.\\{\\[]")[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos":
                    if (this.vehicle != null) {
                        nbt.put("Pos", newDoubleList(this.vehicle.getX(), entity.getY(), this.vehicle.getZ()));
                    } else {
                        nbt.put("Pos", this.newDoubleList(entity.getX(), entity.getY(), entity.getZ()));
                    }
                    break;
                case "Motion":
                    Vec3 vec3d = entity.getDeltaMovement();
                    nbt.put("Motion", this.newDoubleList(vec3d.x, vec3d.y, vec3d.z));
                    break;
                case "Rotation":
                    nbt.put("Rotation", this.newFloatList(entity.getYRot(), entity.getXRot()));
                    break;
                case "FallDistance":
                    nbt.putFloat("FallDistance", entity.fallDistance);
                    break;
                case "Fire":
                    nbt.putShort("Fire", (short) this.remainingFireTicks);
                    break;
                case "Air":
                    nbt.putShort("Air", (short) entity.getAirSupply());
                    break;
                case "OnGround":
                    nbt.putBoolean("OnGround", this.onGround);
                    break;
                case "Invulnerable":
                    nbt.putBoolean("Invulnerable", this.invulnerable);
                    break;
                case "PortalCooldown":
                    nbt.putInt("PortalCooldown", this.portalCooldown);
                    break;
                case "UUID":
                    nbt.putUUID("UUID", entity.getUUID());
                    break;
                case "CustomName":
                    Component text = entity.getCustomName();
                    if (text != null) {
                        nbt.putString("CustomName", Component.Serializer.toJson(text, this.registryAccess()));
                    }
                    break;
                case "CustomNameVisible":
                    if (entity.isCustomNameVisible()) {
                        nbt.putBoolean("CustomNameVisible", entity.isCustomNameVisible());
                    }
                    break;
                case "Silent":
                    if (entity.isSilent()) {
                        nbt.putBoolean("Silent", entity.isSilent());
                    }
                    break;
                case "NoGravity":
                    if (entity.isNoGravity()) {
                        nbt.putBoolean("NoGravity", entity.isNoGravity());
                    }
                    break;
                case "Glowing":
                    if (this.hasGlowingTag) {
                        nbt.putBoolean("Glowing", true);
                    }
                    break;
                case "TicksFrozen":
                    int i = entity.getTicksFrozen();
                    if (i > 0) {
                        nbt.putInt("TicksFrozen", entity.getTicksFrozen());
                    }
                    break;
                case "HasVisualFire":
                    if (this.hasVisualFire) {
                        nbt.putBoolean("HasVisualFire", this.hasVisualFire);
                    }
                    break;
                case "Tags":
                    ListTag nbtList;
                    Iterator<?> var6;
                    if (!this.tags.isEmpty()) {
                        nbtList = new ListTag();
                        var6 = this.tags.iterator();

                        while (var6.hasNext()) {
                            String string = (String) var6.next();
                            nbtList.add(StringTag.valueOf(string));
                        }

                        nbt.put("Tags", nbtList);
                    }
                    break;
                case "Passengers":
                    if (entity.isVehicle()) {
                        nbtList = new ListTag();
                        var6 = entity.getPassengers().iterator();

                        while (var6.hasNext()) {
                            Entity riding_entity = (Entity) var6.next();
                            CompoundTag CompoundTag = new CompoundTag();
                            if (riding_entity.saveAsPassenger(CompoundTag)) {
                                nbtList.add(CompoundTag);
                            }
                        }

                        if (!nbtList.isEmpty()) {
                            nbt.put("Passengers", nbtList);
                        }
                    }
                    break;
                default:
                    if (this.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt))
                        return nbt;
                    else
                        return null;
            }
            return nbt;
        } catch (Throwable var9) {
            CrashReport crashReport = CrashReport.forThrowable(var9, "Saving entity NBT");
            CrashReportCategory crashReportSection = crashReport.addCategory("Entity being saved");
            entity.fillCrashReportCategory(crashReportSection);
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean loadFiltered(CompoundTag nbt, String path) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos" -> {
                    ListTag nbtList = nbt.getList("Pos", 6);
                    entity.setPosRaw(Mth.clamp(nbtList.getDouble(0), -3.0000512E7, 3.0000512E7), Mth.clamp(nbtList.getDouble(1), -2.0E7, 2.0E7), Mth.clamp(nbtList.getDouble(2), -3.0000512E7, 3.0000512E7));
                    entity.setOldPosAndRot();
                    if (Double.isFinite(entity.getX()) && Double.isFinite(entity.getY()) && Double.isFinite(entity.getZ())) {
                        this.reapplyPosition();
                    } else {
                        throw new IllegalStateException("Entity has invalid position");
                    }
                }
                case "Motion" -> {
                    ListTag nbtList2 = nbt.getList("Motion", 6);
                    double d = nbtList2.getDouble(0);
                    double e = nbtList2.getDouble(1);
                    double f = nbtList2.getDouble(2);
                    entity.setDeltaMovement(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(e) > 10.0 ? 0.0 : e, Math.abs(f) > 10.0 ? 0.0 : f);
                }
                case "Rotation" -> {
                    ListTag nbtList3 = nbt.getList("Rotation", 5);
                    entity.setYRot(nbtList3.getFloat(0));
                    entity.setXRot(nbtList3.getFloat(1));
                    entity.setOldPosAndRot();
                    entity.setYHeadRot(entity.getYRot());
                    entity.setYBodyRot(entity.getYRot());
                    if (Double.isFinite(entity.getYRot()) && Double.isFinite(entity.getXRot())) {
                        this.reapplyPosition();
                        this.setRot(entity.getYRot(), entity.getXRot());
                    } else {
                        throw new IllegalStateException("Entity has invalid rotation");
                    }
                }
                case "FallDistance" -> this.fallDistance = nbt.getFloat("FallDistance");
                case "Fire" -> this.remainingFireTicks = nbt.getShort("Fire");
                case "Air" -> entity.setAirSupply(nbt.getShort("Air"));
                case "OnGround" -> this.onGround = nbt.getBoolean("OnGround");
                case "Invulnerable" -> this.invulnerable = nbt.getBoolean("Invulnerable");
                case "PortalCooldown" -> this.portalCooldown = nbt.getInt("PortalCooldown");
                case "UUID" -> {
                    this.uuid = nbt.getUUID("UUID");
                    this.stringUUID = this.uuid.toString();
                }
                case "CustomName" -> {
                    if (nbt.contains("CustomName", 8)) {
                        String string = nbt.getString("CustomName");
                        try {
                            entity.setCustomName(Component.Serializer.fromJson(string, this.registryAccess()));
                        } catch (Exception var16) {
                            LOGGER.warn("Failed to parse entity custom name {}", string, var16);
                        }
                    } else {
                        this.entityData.set(DATA_CUSTOM_NAME, Optional.empty());
                    }
                }
                case "CustomNameVisible" -> entity.setCustomNameVisible(nbt.getBoolean("CustomNameVisible"));
                case "Silent" -> entity.setSilent(nbt.getBoolean("Silent"));
                case "NoGravity" -> entity.setNoGravity(nbt.getBoolean("NoGravity"));
                case "Glowing" -> entity.setGlowingTag(nbt.getBoolean("Glowing"));
                case "TicksFrozen" -> entity.setTicksFrozen(nbt.getInt("TicksFrozen"));
                case "HasVisualFire" -> this.hasVisualFire = nbt.getBoolean("HasVisualFire");
                case "Tags" -> {
                    this.tags.clear();
                    ListTag nbtList4 = nbt.getList("Tags", 8);
                    int i = Math.min(nbtList4.size(), 1024);
                    for (int j = 0; j < i; ++j) {
                        this.tags.add(nbtList4.getString(j));
                    }
                    if (this.level instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index) {
                        for (int j = 0; j < i; ++j) {
                            index.addEntityToTagMap(nbtList4.getString(j), (Entity) (Object) this);
                        }
                    }
                }
                default -> {
                    if (this.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
                        if (this.repositionEntityAfterLoad())
                            this.reapplyPosition();
//                        if (topLevelNbt.equals("ArmorItems") || topLevelNbt.equals("HandItems"))
//                            IntegrationRouter.triggerEquipmentUpdate(this);
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
