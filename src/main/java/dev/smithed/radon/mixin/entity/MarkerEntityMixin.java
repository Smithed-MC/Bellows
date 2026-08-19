package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MarkerEntity.class)
public abstract class MarkerEntityMixin extends EntityMixin implements ICustomNBTMixin {
    @Shadow NbtCompound data;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        if (topLevelNbt.equals("data")) {
            nbt.put("data", this.data.copy());
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        if (topLevelNbt.equals("data")) {
            this.data = nbt.getCompound("data").copy();
        } else {
            return false;
        }
        return true;
    }
}
