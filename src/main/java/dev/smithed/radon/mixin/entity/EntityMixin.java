package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEntityIndexExtender;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IServerWorldExtender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityMixin, ICustomNBTMixin {

    @Shadow
    @Final
    static TrackedData<EntityPose> POSE;
    @Shadow
    @Final
    Random random;
    @Shadow
    World world;
    @Shadow
    DataTracker dataTracker;
    @Shadow
    Entity vehicle;
    @Shadow
    int fireTicks;
    @Shadow
    boolean onGround;
    @Shadow
    boolean invulnerable;
    @Shadow
    int portalCooldown;
    @Shadow
    boolean glowing;
    @Shadow
    boolean hasVisualFire;
    @Shadow
    Set<String> commandTags;
    @Shadow
    UUID uuid;
    @Shadow
    float fallDistance;
    @Shadow
    String uuidString;
    @Shadow
    boolean firstUpdate;

    @Shadow
    abstract void refreshPosition();

    @Shadow
    abstract void setRotation(float yaw, float pitch);

    @Shadow
    abstract boolean shouldSetPositionOnLoad();

    @Shadow
    abstract NbtList toNbtList(double... values);

    @Shadow
    abstract NbtList toNbtList(float... values);

    @Shadow
    abstract void setFlag(int index, boolean value);


    @Shadow
    protected abstract DynamicRegistryManager getRegistryManager();

    /**
     * @author ImCoolYeah105
     * @reason 1 line overwrite
     * Add entity to tag cache when a tag is added
     */
    @Overwrite
    public boolean addCommandTag(String tag) {
        if (this.commandTags.size() < 1024 && this.commandTags.add(tag)) {
            if (this.world instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
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
    public boolean removeCommandTag(String tag) {
        if (this.commandTags.remove(tag)) {
            if (this.world instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
                index.removeEntityFromTagMap(tag, (Entity) (Object) this);
            return true;
        }
        return false;
    }

    /**
     * remove entity from tag cache completely when tags are cleared
     */
    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void radon_readNbt(CallbackInfo ci) {
        if (this.world instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index)
            this.commandTags.forEach(tag -> index.removeEntityFromTagMap(tag, (Entity) (Object) this));
    }

    /**
     * add entity to tag cache when tags are added via NBT data
     */
    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void radon_readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Tags", 9) && this.world instanceof IServerWorldExtender world && world.getEntityIndex().get(uuid) != null && world.getEntityIndex() instanceof IEntityIndexExtender index) {
            NbtList nbtList4 = nbt.getList("Tags", 8);
            int i = Math.min(nbtList4.size(), 1024);

            for (int j = 0; j < i; ++j) {
                index.addEntityToTagMap(nbtList4.getString(j), (Entity) (Object) this);
            }
        }
    }

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        return false;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        return false;
    }

    @Override
    public NbtCompound writeNbtFiltered(NbtCompound nbt, String path, RegistryWrapper.WrapperLookup registries) {
        String topLevelNbt = path.split("[\\.\\{\\[]")[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos":
                    if (this.vehicle != null) {
                        nbt.put("Pos", toNbtList(this.vehicle.getX(), entity.getY(), this.vehicle.getZ()));
                    } else {
                        nbt.put("Pos", this.toNbtList(entity.getX(), entity.getY(), entity.getZ()));
                    }
                    break;
                case "Motion":
                    Vec3d vec3d = entity.getVelocity();
                    nbt.put("Motion", this.toNbtList(vec3d.x, vec3d.y, vec3d.z));
                    break;
                case "Rotation":
                    nbt.put("Rotation", this.toNbtList(entity.getYaw(), entity.getPitch()));
                    break;
                case "FallDistance":
                    nbt.putFloat("FallDistance", entity.fallDistance);
                    break;
                case "Fire":
                    nbt.putShort("Fire", (short) this.fireTicks);
                    break;
                case "Air":
                    nbt.putShort("Air", (short) entity.getAir());
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
                    nbt.putUuid("UUID", entity.getUuid());
                    break;
                case "CustomName":
                    Text text = entity.getCustomName();
                    if (text != null) {
                        nbt.putString("CustomName", Text.Serialization.toJsonString(text, registries));
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
                    if (entity.hasNoGravity()) {
                        nbt.putBoolean("NoGravity", entity.hasNoGravity());
                    }
                    break;
                case "Glowing":
                    if (this.glowing) {
                        nbt.putBoolean("Glowing", true);
                    }
                    break;
                case "TicksFrozen":
                    int i = entity.getFrozenTicks();
                    if (i > 0) {
                        nbt.putInt("TicksFrozen", entity.getFrozenTicks());
                    }
                    break;
                case "HasVisualFire":
                    if (this.hasVisualFire) {
                        nbt.putBoolean("HasVisualFire", this.hasVisualFire);
                    }
                    break;
                case "Tags":
                    NbtList nbtList;
                    Iterator<?> var6;
                    if (!this.commandTags.isEmpty()) {
                        nbtList = new NbtList();
                        var6 = this.commandTags.iterator();

                        while (var6.hasNext()) {
                            String string = (String) var6.next();
                            nbtList.add(NbtString.of(string));
                        }

                        nbt.put("Tags", nbtList);
                    }
                    break;
                case "Passengers":
                    if (entity.hasPassengers()) {
                        nbtList = new NbtList();
                        var6 = entity.getPassengerList().iterator();

                        while (var6.hasNext()) {
                            Entity riding_entity = (Entity) var6.next();
                            NbtCompound nbtCompound = new NbtCompound();
                            if (riding_entity.saveSelfNbt(nbtCompound)) {
                                nbtList.add(nbtCompound);
                            }
                        }

                        if (!nbtList.isEmpty()) {
                            nbt.put("Passengers", nbtList);
                        }
                    }
                    break;
                default:
                    if (this.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries))
                        return nbt;
                    else
                        return null;
            }
            return nbt;
        } catch (Throwable var9) {
            CrashReport crashReport = CrashReport.create(var9, "Saving entity NBT");
            CrashReportSection crashReportSection = crashReport.addElement("Entity being saved");
            entity.populateCrashReport(crashReportSection);
            throw new CrashException(crashReport);
        }
    }

    @Override
    public boolean readNbtFiltered(NbtCompound nbt, String path, RegistryWrapper.WrapperLookup registries) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        Entity entity = ((Entity) (Object) this);

        try {
            switch (topLevelNbt) {
                case "Pos" -> {
                    NbtList nbtList = nbt.getList("Pos", 6);
                    entity.setPos(MathHelper.clamp(nbtList.getDouble(0), -3.0000512E7, 3.0000512E7), MathHelper.clamp(nbtList.getDouble(1), -2.0E7, 2.0E7), MathHelper.clamp(nbtList.getDouble(2), -3.0000512E7, 3.0000512E7));
                    entity.resetPosition();
                    if (Double.isFinite(entity.getX()) && Double.isFinite(entity.getY()) && Double.isFinite(entity.getZ())) {
                        this.refreshPosition();
                    } else {
                        throw new IllegalStateException("Entity has invalid position");
                    }
                }
                case "Motion" -> {
                    NbtList nbtList2 = nbt.getList("Motion", 6);
                    double d = nbtList2.getDouble(0);
                    double e = nbtList2.getDouble(1);
                    double f = nbtList2.getDouble(2);
                    entity.setVelocity(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(e) > 10.0 ? 0.0 : e, Math.abs(f) > 10.0 ? 0.0 : f);
                }
                case "Rotation" -> {
                    NbtList nbtList3 = nbt.getList("Rotation", 5);
                    entity.setYaw(nbtList3.getFloat(0));
                    entity.setPitch(nbtList3.getFloat(1));
                    entity.resetPosition();
                    entity.setHeadYaw(entity.getYaw());
                    entity.setBodyYaw(entity.getYaw());
                    if (Double.isFinite(entity.getYaw()) && Double.isFinite(entity.getPitch())) {
                        this.refreshPosition();
                        this.setRotation(entity.getYaw(), entity.getPitch());
                    } else {
                        throw new IllegalStateException("Entity has invalid rotation");
                    }
                }
                case "FallDistance" -> this.fallDistance = nbt.getFloat("FallDistance");
                case "Fire" -> this.fireTicks = nbt.getShort("Fire");
                case "Air" -> entity.setAir(nbt.getShort("Air"));
                case "OnGround" -> this.onGround = nbt.getBoolean("OnGround");
                case "Invulnerable" -> this.invulnerable = nbt.getBoolean("Invulnerable");
                case "PortalCooldown" -> this.portalCooldown = nbt.getInt("PortalCooldown");
                case "UUID" -> {
                    this.uuid = nbt.getUuid("UUID");
                    this.uuidString = this.uuid.toString();
                }
                case "CustomName" -> {
                    String string = nbt.getString("CustomName");
                    try {
                        entity.setCustomName(Text.Serialization.fromJson(string, registries));
                    } catch (Exception var16) {
                        Radon.LOGGER.warn("Failed to parse entity custom name {}", string, var16);
                    }
                }
                case "CustomNameVisible" -> entity.setCustomNameVisible(nbt.getBoolean("CustomNameVisible"));
                case "Silent" -> entity.setSilent(nbt.getBoolean("Silent"));
                case "NoGravity" -> entity.setNoGravity(nbt.getBoolean("NoGravity"));
                case "Glowing" -> entity.setGlowing(nbt.getBoolean("Glowing"));
                case "TicksFrozen" -> entity.setFrozenTicks(nbt.getInt("TicksFrozen"));
                case "HasVisualFire" -> this.hasVisualFire = nbt.getBoolean("HasVisualFire");
                case "Tags" -> {
                    this.commandTags.clear();
                    NbtList nbtList4 = nbt.getList("Tags", 8);
                    int i = Math.min(nbtList4.size(), 1024);
                    for (int j = 0; j < i; ++j) {
                        this.commandTags.add(nbtList4.getString(j));
                    }
                    if (this.world instanceof IServerWorldExtender world && world.getEntityIndex() instanceof IEntityIndexExtender index) {
                        for (int j = 0; j < i; ++j) {
                            index.addEntityToTagMap(nbtList4.getString(j), (Entity) (Object) this);
                        }
                    }
                }
                default -> {
                    if (this.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {
                        if (this.shouldSetPositionOnLoad())
                            this.refreshPosition();
//                        if (topLevelNbt.equals("ArmorItems") || topLevelNbt.equals("HandItems"))
//                            IntegrationRouter.triggerEquipmentUpdate(this);
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (Throwable var17) {
            CrashReport crashReport = CrashReport.create(var17, "Loading entity NBT");
            CrashReportSection crashReportSection = crashReport.addElement("Entity being loaded");
            entity.populateCrashReport(crashReportSection);
            throw new CrashException(crashReport);
        }
    }

}
