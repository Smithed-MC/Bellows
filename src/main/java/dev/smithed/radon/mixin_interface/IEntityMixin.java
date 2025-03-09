package dev.smithed.radon.mixin_interface;

import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public interface IEntityMixin {
    NbtCompound writeNbtFiltered(NbtCompound nbt, String path);
    boolean readNbtFiltered(NbtCompound nbt, String path);

}
