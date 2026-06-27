package dev.smithed.radon.mixin.entity;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.class)
public abstract class DisplayEntityMixin extends EntityMixin {

    @Shadow @Final protected static Logger LOGGER;
    @Shadow private static Transformation createTransformation(SynchedEntityData dataTracker) { return null; }

    @Override
    public boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.radon_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Display entity = ((Display) (Object) this);

        switch (topLevelNbt) {
            case "transformation" -> output.store("transformation", Transformation.EXTENDED_CODEC, createTransformation(entity.getEntityData()));
            case "billboard" -> output.store("billboard", Display.BillboardConstraints.CODEC, entity.getBillboardConstraints());
            case "interpolation_duration" -> output.putInt("interpolation_duration", entity.getTransformationInterpolationDuration());
            case "teleport_duration" -> output.putInt("teleport_duration", entity.getPosRotInterpolationDuration());
            case "view_range" -> output.putFloat("view_range", entity.getViewRange());
            case "shadow_radius" -> output.putFloat("shadow_radius", entity.getShadowRadius());
            case "shadow_strength" -> output.putFloat("shadow_strength", entity.getShadowStrength());
            case "width" -> output.putFloat("width", entity.getWidth());
            case "height" -> output.putFloat("height", entity.getHeight());
            case "glow_color_override" -> output.putInt("glow_color_override", entity.getGlowColorOverride());
            case "brightness" -> output.storeNullable("brightness", Brightness.CODEC, entity.getBrightnessOverride());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean radon_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.radon_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Display entity = ((Display) (Object) this);

        switch (topLevelNbt) {
            case "transformation" -> entity.setTransformation(input.read("transformation", Transformation.EXTENDED_CODEC).orElse(Transformation.IDENTITY));
            case "interpolation_duration" -> entity.setTransformationInterpolationDuration(input.getIntOr("interpolation_duration", 0));
            case "start_interpolation" -> entity.setTransformationInterpolationDelay(input.getIntOr("start_interpolation", 0));
            case "teleport_duration" -> {
                int teleportDuration = input.getIntOr("teleport_duration", 0);
                entity.setPosRotInterpolationDuration(Mth.clamp(teleportDuration, 0, 59));
            }
            case "billboard" -> entity.setBillboardConstraints(input.read("billboard", Display.BillboardConstraints.CODEC).orElse(Display.BillboardConstraints.FIXED));
            case "view_range" -> entity.setViewRange(input.getFloatOr("view_range", 1.0F));
            case "shadow_radius" -> entity.setShadowRadius(input.getFloatOr("shadow_radius", 0.0F));
            case "shadow_strength" -> entity.setShadowStrength(input.getFloatOr("shadow_strength", 1.0F));
            case "width" -> entity.setWidth(input.getFloatOr("width", 0.0F));
            case "height" -> entity.setHeight(input.getFloatOr("height", 0.0F));
            case "glow_color_override" -> entity.setGlowColorOverride(input.getIntOr("glow_color_override", -1));
            case "brightness" -> entity.setBrightnessOverride((Brightness)input.read("brightness", Brightness.CODEC).orElse(null));
            default -> {
                return false;
            }
        }
        return true;
    }
}
