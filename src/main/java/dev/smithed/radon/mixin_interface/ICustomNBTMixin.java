package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.CompoundTag;

public interface ICustomNBTMixin {

    boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt);

    boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt);

}
