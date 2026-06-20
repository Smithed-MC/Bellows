package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Marker.class)
public abstract class MarkerMixin extends EntityMixin implements ICustomNBTMixin {
    @Shadow CompoundTag data;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
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
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
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
