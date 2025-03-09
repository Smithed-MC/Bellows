package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

public interface IEnderChestInventoryExtender {

    NbtList toNbtListFiltered(String nbt, RegistryWrapper.WrapperLookup registries);

}
