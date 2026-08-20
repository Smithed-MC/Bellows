package net.smithed.bellows.mixin.nbt.entity;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Brightness;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.smithed.bellows.mixin_interface.nbt.DisplayEntityExtender;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Display.class)
public abstract class DisplayMixin extends EntityMixin implements DisplayEntityExtender {

    @Shadow @Final private static EntityDataAccessor<Vector3fc> DATA_TRANSLATION_ID;
    @Shadow @Final private static EntityDataAccessor<Vector3fc> DATA_SCALE_ID;
    @Shadow @Final private static EntityDataAccessor<Quaternionfc> DATA_LEFT_ROTATION_ID;
    @Shadow @Final private static EntityDataAccessor<Quaternionfc> DATA_RIGHT_ROTATION_ID;

    @Shadow @Final protected static Logger LOGGER;

    @Shadow @Final
    public abstract void setTransformationInterpolationDelay(final int ticks);

    @Shadow
    public abstract void setTransformation(Transformation transformation);

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void bellows_init(EntityType<?> type, Level level, CallbackInfo ci) {
        setTransformation(Transformation.IDENTITY);
    }

    /**
     * Disables culling calculations on the server, as these are only used on the client.
     */
    @Inject(method = "updateCulling()V", at = @At("HEAD"), cancellable = true)
    private void bellows_culling(CallbackInfo ci) {
        Display entity = ((Display) (Object) this);
        if(!entity.level().isClientSide()) {
            ci.cancel();
        }
    }

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Display entity = ((Display) (Object) this);

        switch (topLevelNbt) {
            case "transformation" -> {
                // server side display entity does not use the matrix form of the transformation and only stores the component form.
                // So we bypass creating a new transformation entirely and store each component individually.
                ValueOutput transformation = output.child("transformation");
                transformation.store("translation", ExtraCodecs.VECTOR3F, entityData.get(DATA_TRANSLATION_ID));
                transformation.store("left_rotation", ExtraCodecs.QUATERNIONF, entityData.get(DATA_LEFT_ROTATION_ID));
                transformation.store("scale", ExtraCodecs.VECTOR3F, entityData.get(DATA_SCALE_ID));
                transformation.store("right_rotation", ExtraCodecs.QUATERNIONF, entityData.get(DATA_RIGHT_ROTATION_ID));
            }
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
            case "start_interpolation" -> {} // special write-only case. This will make it return true, even without NBT added.
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
        Display entity = ((Display) (Object) this);

        switch (topLevelNbt) {
            case "transformation" -> {
                Optional<ValueInput> transformation = input.child("transformation");
                if(transformation.isPresent()) {
                    Optional<Vector3fc> translation = transformation.get().read("translation", ExtraCodecs.VECTOR3F);
                    Optional<Quaternionfc> left_rotation = transformation.get().read("left_rotation", ExtraCodecs.QUATERNIONF);
                    Optional<Vector3fc> scale = transformation.get().read("scale", ExtraCodecs.VECTOR3F);
                    Optional<Quaternionfc> right_rotation = transformation.get().read("right_rotation", ExtraCodecs.QUATERNIONF);

                    if(translation.isPresent() && !this.entityData.get(DATA_TRANSLATION_ID).equals(translation.get())) {
                        this.entityData.set(DATA_TRANSLATION_ID, translation.get());
                    }
                    if(left_rotation.isPresent() && !this.entityData.get(DATA_LEFT_ROTATION_ID).equals(left_rotation.get())) {
                        this.entityData.set(DATA_LEFT_ROTATION_ID, left_rotation.get());
                    }
                    if(scale.isPresent() && !this.entityData.get(DATA_SCALE_ID).equals(scale.get())) {
                        this.entityData.set(DATA_SCALE_ID, scale.get());
                    }
                    if(right_rotation.isPresent() && !this.entityData.get(DATA_RIGHT_ROTATION_ID).equals(right_rotation.get())) {
                        this.entityData.set(DATA_RIGHT_ROTATION_ID, right_rotation.get());
                    }
                    entity.setTransformationInterpolationDelay(input.getIntOr("start_interpolation", 0));
                } else {
                    this.setTransformation(input.read("transformation", Transformation.EXTENDED_CODEC).orElse(Transformation.IDENTITY));
                }
            }
            case "interpolation_duration" -> entity.setTransformationInterpolationDuration(input.getIntOr("interpolation_duration", 0));
            case "start_interpolation" -> {}
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
            case "brightness" -> entity.setBrightnessOverride(input.read("brightness", Brightness.CODEC).orElse(null));
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void bellows_setTranslation(Vector3fc translation) {
        if(!this.entityData.get(DATA_TRANSLATION_ID).equals(translation)) {
            this.entityData.set(DATA_TRANSLATION_ID, translation);
            this.setTransformationInterpolationDelay(0);
        }
    }

    @Override
    public void bellows_setLeftRotation(Quaternionfc leftRotation) {
        if(!this.entityData.get(DATA_LEFT_ROTATION_ID).equals(leftRotation)) {
            this.entityData.set(DATA_LEFT_ROTATION_ID, leftRotation);
            this.setTransformationInterpolationDelay(0);
        }
    }

    @Override
    public void bellows_setScale(Vector3fc scale) {
        if(!this.entityData.get(DATA_SCALE_ID).equals(scale)) {
            this.entityData.set(DATA_SCALE_ID, scale);
            this.setTransformationInterpolationDelay(0);
        }
    }

    @Override
    public void bellows_setRightRotation(Quaternionfc rightRotation) {
        if(!this.entityData.get(DATA_RIGHT_ROTATION_ID).equals(rightRotation)) {
            this.entityData.set(DATA_RIGHT_ROTATION_ID, rightRotation);
            this.setTransformationInterpolationDelay(0);
        }
    }
}
