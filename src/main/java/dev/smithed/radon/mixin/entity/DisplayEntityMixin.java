package dev.smithed.radon.mixin.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(DisplayEntity.class)
public abstract class DisplayEntityMixin extends EntityMixin {

    @Shadow @Final static Logger LOGGER;

    @Shadow static AffineTransformation getTransformation(DataTracker dataTracker) { return null; }
    @Shadow abstract Brightness getBrightnessUnpacked();
    @Shadow abstract int getInterpolationDuration();
    @Shadow abstract float getViewRange();
    @Shadow abstract float getShadowRadius();
    @Shadow abstract float getShadowStrength();
    @Shadow abstract float getDisplayWidth();
    @Shadow abstract float getDisplayHeight();
    @Shadow abstract int getGlowColorOverride();
    @Shadow @Final abstract DisplayEntity.BillboardMode getBillboardMode();
    @Shadow @Final abstract int getTeleportDuration();

    @Shadow abstract void setTransformation(AffineTransformation transformation);
    @Shadow abstract void setInterpolationDuration(int interpolationDuration);
    @Shadow abstract void setStartInterpolation(int startInterpolation);
    @Shadow abstract void setBillboardMode(DisplayEntity.BillboardMode billboardMode);
    @Shadow abstract void setBrightness(@Nullable Brightness brightness);
    @Shadow abstract void setViewRange(float viewRange);
    @Shadow abstract void setShadowRadius(float shadowRadius);
    @Shadow abstract void setDisplayWidth(float width);
    @Shadow abstract void setDisplayHeight(float height);
    @Shadow abstract void setShadowStrength(float shadowStrength);
    @Shadow abstract void setGlowColorOverride(int glowColorOverride);
    @Shadow @Final abstract void setTeleportDuration(int teleportDuration);

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "transformation" ->
                    AffineTransformation.ANY_CODEC.encodeStart(NbtOps.INSTANCE, getTransformation(this.dataTracker)).ifSuccess((transformations) -> {
                        nbt.put("transformation", transformations);
                    });
            case "billboard" ->
                    DisplayEntity.BillboardMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardMode()).ifSuccess((billboard) -> {
                        nbt.put("billboard", billboard);
                    });
            case "interpolation_duration" -> nbt.putInt("interpolation_duration", this.getInterpolationDuration());
            case "teleport_duration" -> nbt.putInt("teleport_duration", this.getTeleportDuration());
            case "view_range" -> nbt.putFloat("view_range", this.getViewRange());
            case "shadow_radius" -> nbt.putFloat("shadow_radius", this.getShadowRadius());
            case "shadow_strength" -> nbt.putFloat("shadow_strength", this.getShadowStrength());
            case "width" -> nbt.putFloat("width", this.getDisplayWidth());
            case "height" -> nbt.putFloat("height", this.getDisplayHeight());
            case "glow_color_override" -> nbt.putInt("glow_color_override", this.getGlowColorOverride());
            case "brightness" -> {
                Brightness brightness = this.getBrightnessUnpacked();
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
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "transformation" -> {
                DataResult<Pair<AffineTransformation, NbtElement>> var10000;
                Logger var10002;
                if (nbt.contains("transformation")) {
                    var10000 = AffineTransformation.ANY_CODEC.decode(NbtOps.INSTANCE, nbt.get("transformation"));
                    var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(
                            Util.addPrefix("Display entity", var10002::error))
                            .ifPresent((pair) -> this.setTransformation(pair.getFirst())
                    );
                }
            }
            case "interpolation_duration" -> {
                int i;
                if (nbt.contains("interpolation_duration", 99)) {
                    i = nbt.getInt("interpolation_duration");
                    this.setInterpolationDuration(i);
                }
            }
            case "start_interpolation" -> {
                int i;
                if (nbt.contains("start_interpolation", 99)) {
                    i = nbt.getInt("start_interpolation");
                    this.setStartInterpolation(i);
                }
            }
            case "teleport_duration" -> {
                int i;
                if (nbt.contains("teleport_duration", 99)) {
                    i = nbt.getInt("teleport_duration");
                    this.setTeleportDuration(MathHelper.clamp(i, 0, 59));
                }
            }
            case "billboard" -> {
                if (nbt.contains("billboard", 8)) {
                    DataResult<Pair<DisplayEntity.BillboardMode, NbtElement>> var10000 =
                        DisplayEntity.BillboardMode.CODEC.decode(NbtOps.INSTANCE, nbt.get("billboard"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((pair) -> {
                        this.setBillboardMode(pair.getFirst());
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
                    this.setDisplayWidth(nbt.getFloat("width"));
                }}
            case "height" -> {
                if (nbt.contains("height", 99)) {
                    this.setDisplayHeight(nbt.getFloat("height"));
                }}
            case "glow_color_override" -> {
                if (nbt.contains("glow_color_override", 99)) {
                    this.setGlowColorOverride(nbt.getInt("glow_color_override"));
                }}
            case "brightness" -> {
                if (nbt.contains("brightness", 10)) {
                    DataResult<Pair<Brightness, NbtElement>> var10000
                            = Brightness.CODEC.decode(NbtOps.INSTANCE, nbt.get("brightness"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((pair) -> {
                        this.setBrightness(pair.getFirst());
                    });
                } else {
                    this.setBrightness(null);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
