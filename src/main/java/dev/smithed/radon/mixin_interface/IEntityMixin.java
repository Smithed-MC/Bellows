package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.CompoundTag;

public interface IEntityMixin {
    CompoundTag saveWithoutIdFiltered(CompoundTag nbt, String path);
    boolean loadFiltered(CompoundTag nbt, String path);

}
