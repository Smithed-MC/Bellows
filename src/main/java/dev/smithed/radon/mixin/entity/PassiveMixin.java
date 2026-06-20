package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableMob.class)
public abstract class PassiveMixin extends MobMixin implements ICustomNBTMixin {

    @Shadow int forcedAge;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        AgeableMob entity = ((AgeableMob)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Age" -> nbt.putInt("Age", entity.getAge());
            case "ForcedAge" -> nbt.putInt("ForcedAge", this.forcedAge);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        AgeableMob entity = ((AgeableMob)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Age" -> entity.setAge(nbt.getInt("Age"));
            case "ForcedAge" -> this.forcedAge = nbt.getInt("ForcedAge");
            default -> {
                return false;
            }
        }
        return true;
    }
}
