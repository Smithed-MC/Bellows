package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MarkerEntity.class)
public abstract class MarkerEntityMixin extends EntityMixin implements ICustomNBTMixin {
    @Shadow NbtCompound data;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MarkerEntity entity = ((MarkerEntity) (Object) this);
        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries)) {
            if (topLevelNbt.equals("data")) {
                nbt.put("data", this.data.copy());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MarkerEntity entity = ((MarkerEntity)(Object)this);
        if(!nbt.contains(topLevelNbt))
            return false;
        if (topLevelNbt.equals("data")) {
            this.data = nbt.getCompound("data");
        } else {
            return false;
        }
        return true;
    }
}
