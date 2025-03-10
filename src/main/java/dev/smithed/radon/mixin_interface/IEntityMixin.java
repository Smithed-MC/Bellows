package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.NbtCompound;

public interface IEntityMixin {
    NbtCompound writeNbtFiltered(NbtCompound nbt, String path);
    boolean readNbtFiltered(NbtCompound nbt, String path);

}
