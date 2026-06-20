package dev.smithed.radon.mixin.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Display.class)
public abstract class DisplayEntityMixin extends EntityMixin {

    @Shadow @Final static Logger LOGGER;

    @Shadow static Transformation createTransformation(SynchedEntityData dataTracker) { return null; }
    @Shadow abstract Brightness getBrightnessOverride();
    @Shadow abstract int getTransformationInterpolationDuration();
    @Shadow abstract float getViewRange();
    @Shadow abstract float getShadowRadius();
    @Shadow abstract float getShadowStrength();
    @Shadow abstract float getWidth();
    @Shadow abstract float getHeight();
    @Shadow abstract int getGlowColorOverride();
    @Shadow @Final abstract Display.BillboardConstraints getBillboardConstraints();
    @Shadow @Final abstract int getPosRotInterpolationDuration();

    @Shadow abstract void setTransformation(Transformation transformation);
    @Shadow abstract void setTransformationInterpolationDuration(int interpolationDuration);
    @Shadow abstract void setTransformationInterpolationDelay(int startInterpolation);
    @Shadow abstract void setBillboardConstraints(Display.BillboardConstraints billboardMode);
    @Shadow abstract void setBrightnessOverride(@Nullable Brightness brightness);
    @Shadow abstract void setViewRange(float viewRange);
    @Shadow abstract void setShadowRadius(float shadowRadius);
    @Shadow abstract void setWidth(float width);
    @Shadow abstract void setHeight(float height);
    @Shadow abstract void setShadowStrength(float shadowStrength);
    @Shadow abstract void setGlowColorOverride(int glowColorOverride);
    @Shadow @Final abstract void setPosRotInterpolationDuration(int teleportDuration);

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "transformation" ->
                    Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, createTransformation(this.entityData)).ifSuccess((transformations) -> {
                        nbt.put("transformation", transformations);
                    });
            case "billboard" ->
                    Display.BillboardConstraints.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardConstraints()).ifSuccess((billboard) -> {
                        nbt.put("billboard", billboard);
                    });
            case "interpolation_duration" -> nbt.putInt("interpolation_duration", this.getTransformationInterpolationDuration());
            case "teleport_duration" -> nbt.putInt("teleport_duration", this.getPosRotInterpolationDuration());
            case "view_range" -> nbt.putFloat("view_range", this.getViewRange());
            case "shadow_radius" -> nbt.putFloat("shadow_radius", this.getShadowRadius());
            case "shadow_strength" -> nbt.putFloat("shadow_strength", this.getShadowStrength());
            case "width" -> nbt.putFloat("width", this.getWidth());
            case "height" -> nbt.putFloat("height", this.getHeight());
            case "glow_color_override" -> nbt.putInt("glow_color_override", this.getGlowColorOverride());
            case "brightness" -> {
                Brightness brightness = this.getBrightnessOverride();
                if (brightness != null) {
                    Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).ifSuccess((brightnessx) -> {
                        nbt.put("brightness", brightnessx);
                    });
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "transformation" -> {
                DataResult<Pair<Transformation, Tag>> var10000;
                Logger var10002;
                if (nbt.contains("transformation")) {
                    var10000 = Transformation.EXTENDED_CODEC.decode(NbtOps.INSTANCE, nbt.get("transformation"));
                    var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(
                            Util.prefix("Display entity", var10002::error))
                            .ifPresent((pair) -> this.setTransformation(pair.getFirst())
                    );
                }
            }
            case "interpolation_duration" -> {
                int i;
                if (nbt.contains("interpolation_duration", 99)) {
                    i = nbt.getInt("interpolation_duration");
                    this.setTransformationInterpolationDuration(i);
                }
            }
            case "start_interpolation" -> {
                int i;
                if (nbt.contains("start_interpolation", 99)) {
                    i = nbt.getInt("start_interpolation");
                    this.setTransformationInterpolationDelay(i);
                }
            }
            case "teleport_duration" -> {
                int i;
                if (nbt.contains("teleport_duration", 99)) {
                    i = nbt.getInt("teleport_duration");
                    this.setPosRotInterpolationDuration(Mth.clamp(i, 0, 59));
                }
            }
            case "billboard" -> {
                if (nbt.contains("billboard", 8)) {
                    DataResult<Pair<Display.BillboardConstraints, Tag>> var10000 =
                        Display.BillboardConstraints.CODEC.decode(NbtOps.INSTANCE, nbt.get("billboard"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.prefix("Display entity", var10002::error)).ifPresent((pair) -> {
                        this.setBillboardConstraints(pair.getFirst());
                    });
                }}
            case "view_range" -> {
                if (nbt.contains("view_range", 99)) {
                    this.setViewRange(nbt.getFloat("view_range"));
                }}
            case "shadow_radius" -> {
                if (nbt.contains("shadow_radius", 99)) {
                    this.setShadowRadius(nbt.getFloat("shadow_radius"));
                }}
            case "shadow_strength" -> {
                if (nbt.contains("shadow_strength", 99)) {
                    this.setShadowStrength(nbt.getFloat("shadow_strength"));
                }}
            case "width" -> {
                if (nbt.contains("width", 99)) {
                    this.setWidth(nbt.getFloat("width"));
                }}
            case "height" -> {
                if (nbt.contains("height", 99)) {
                    this.setHeight(nbt.getFloat("height"));
                }}
            case "glow_color_override" -> {
                if (nbt.contains("glow_color_override", 99)) {
                    this.setGlowColorOverride(nbt.getInt("glow_color_override"));
                }}
            case "brightness" -> {
                if (nbt.contains("brightness", 10)) {
                    DataResult<Pair<Brightness, Tag>> var10000
                            = Brightness.CODEC.decode(NbtOps.INSTANCE, nbt.get("brightness"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.prefix("Display entity", var10002::error)).ifPresent((pair) -> {
                        this.setBrightnessOverride(pair.getFirst());
                    });
                } else {
                    this.setBrightnessOverride(null);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
