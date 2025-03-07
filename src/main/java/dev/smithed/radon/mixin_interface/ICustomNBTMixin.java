package dev.smithed.radon.mixin_interface;

import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public interface ICustomNBTMixin {

    boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries);

    boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries);

}
