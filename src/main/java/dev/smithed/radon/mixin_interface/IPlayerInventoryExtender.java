package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.NbtList;

public interface IPlayerInventoryExtender {

    NbtList writeNbtFiltered(NbtList nbtList, String nbt);

}
