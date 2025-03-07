package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

public interface IFilteredNbtList {

    NbtList writeNbtFiltered(NbtList nbtList, String nbt, RegistryWrapper.WrapperLookup registries);

}
