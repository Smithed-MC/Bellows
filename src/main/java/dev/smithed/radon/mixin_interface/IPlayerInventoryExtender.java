package dev.smithed.radon.mixin_interface;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;

public interface IPlayerInventoryExtender {

    void saveWithoutIdFiltered(ValueOutput.TypedOutputList<ItemStackWithSlot> output, String nbt);

}
