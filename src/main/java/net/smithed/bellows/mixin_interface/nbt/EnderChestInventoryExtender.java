package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface EnderChestInventoryExtender {

    void bellows_toNbtListFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);
}
