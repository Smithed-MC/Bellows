package net.smithed.bellows.mixin.entity;

import net.smithed.bellows.mixin_interface.ICustomNBTMixin;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin extends MobMixin implements ICustomNBTMixin {

    @Shadow protected int forcedAge;
    @Shadow protected abstract boolean canBeABaby();
    @Shadow protected abstract void setAgeLocked(final boolean locked);

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        AgeableMob entity = ((AgeableMob)(Object)this);

        switch (topLevelNbt) {
            case "Age" -> {
                if (this.canBeABaby()) {
                    output.putInt("Age", entity.getAge());
                }
            }
            case "ForcedAge" -> {
                if (this.canBeABaby()) {
                    output.putInt("ForcedAge", this.forcedAge);
                }
            }
            case "AgeLocked" -> {
                if (this.canBeABaby()) {
                    output.putBoolean("AgeLocked", entity.isAgeLocked());
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
        AgeableMob entity = ((AgeableMob)(Object)this);

        switch (topLevelNbt) {
            case "Age" -> {
                if (this.canBeABaby()) {
                    entity.setAge(input.getIntOr("Age", 0));
                }
            }
            case "ForcedAge" -> {
                if (this.canBeABaby()) {
                    this.forcedAge = input.getIntOr("ForcedAge", 0);
                }
            }
            case "AgeLocked" -> {
                if (this.canBeABaby()) {
                    this.setAgeLocked(input.getBooleanOr("AgeLocked", false));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
