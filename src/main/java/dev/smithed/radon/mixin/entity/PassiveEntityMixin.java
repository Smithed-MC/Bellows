package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin extends MobEntityMixin implements ICustomNBTMixin {

    @Shadow int forcedAge;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        PassiveEntity entity = ((PassiveEntity)(Object)this);
        if(!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            switch (topLevelNbt) {
                case "Age" -> nbt.putInt("Age", entity.getBreedingAge());
                case "ForcedAge" -> nbt.putInt("ForcedAge", this.forcedAge);
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        PassiveEntity entity = ((PassiveEntity)(Object)this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {

            switch (topLevelNbt) {
                case "Age" -> entity.setBreedingAge(nbt.getInt("Age"));
                case "ForcedAge" -> this.forcedAge = nbt.getInt("ForcedAge");
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
