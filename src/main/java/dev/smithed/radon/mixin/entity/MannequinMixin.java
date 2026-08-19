package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Mannequin.class)
public abstract class MannequinMixin extends LivingEntityMixin {

    @Shadow @Final private static Codec<Byte> LAYERS_CODEC;
    @Shadow @Final private static Component DEFAULT_DESCRIPTION;
    @Shadow @Final private static byte ALL_LAYERS;

    @Shadow private boolean getImmovable() { return false; }
    @Shadow protected @Nullable Component getDescription() { return null; }
    @Shadow private void setProfile(final ResolvableProfile profile) {}
    @Shadow private void setImmovable(final boolean immovable) {}
    @Shadow private void setDescription(final Component description) {}
    @Shadow private void setHideDescription(final boolean hideDescription) {}

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Mannequin entity = ((Mannequin)(Object)this);

        switch (topLevelNbt) {
            case "profile" -> output.store("profile", ResolvableProfile.CODEC, entity.getProfile());
            case "hidden_layers" -> output.store("hidden_layers", LAYERS_CODEC, this.entityData.get(Avatar.DATA_PLAYER_MODE_CUSTOMISATION));
            case "main_hand" -> output.store("main_hand", HumanoidArm.CODEC, entity.getMainArm());
            case "pose" -> output.store("pose", Mannequin.POSE_CODEC, entity.getPose());
            case "immovable" -> output.putBoolean("immovable", this.getImmovable());
            case "description", "hide_description" -> {
                Component description = this.getDescription();
                if (description != null) {
                    if (!description.equals(DEFAULT_DESCRIPTION)) {
                        output.store("description", ComponentSerialization.CODEC, description);
                    }
                } else {
                    output.putBoolean("hide_description", true);
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
        Mannequin entity = ((Mannequin)(Object)this);

        switch (topLevelNbt) {
            case "profile" -> input.read("profile", ResolvableProfile.CODEC).ifPresent(this::setProfile);
            case "hidden_layers" -> this.entityData.set(Avatar.DATA_PLAYER_MODE_CUSTOMISATION, input.read("hidden_layers", LAYERS_CODEC).orElse(ALL_LAYERS));
            case "main_hand" -> entity.setMainArm(input.read("main_hand", HumanoidArm.CODEC).orElse(Avatar.DEFAULT_MAIN_HAND));
            case "pose" -> entity.setPose(input.read("pose", Mannequin.POSE_CODEC).orElse(Pose.STANDING));
            case "immovable" -> this.setImmovable(input.getBooleanOr("immovable", false));
            case "description", "hide_description" -> {
                this.setHideDescription(input.getBooleanOr("hide_description", false));
                this.setDescription(input.read("description", ComponentSerialization.CODEC).orElse(DEFAULT_DESCRIPTION));
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
