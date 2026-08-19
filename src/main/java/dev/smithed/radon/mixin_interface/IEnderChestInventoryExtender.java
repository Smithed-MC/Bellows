package dev.smithed.radon.mixin_interface;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface IEnderChestInventoryExtender {

    void bellows_toNbtListFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);
}
