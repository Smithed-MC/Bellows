package dev.smithed.radon.mixin_interface;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;

public interface IEnderChestInventoryExtender {

    ListTag toNbtListFiltered(String nbt, HolderLookup.Provider registries);

}
