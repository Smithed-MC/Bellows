package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.ListTag;

public interface IPlayerInventoryExtender {

    ListTag saveWithoutIdFiltered(ListTag nbtList, String nbt);

}
