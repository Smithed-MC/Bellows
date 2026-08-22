package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface EnderChestInventoryExtender {

    /**
     * Bypass of PlayerEnderChestContainer::storeAsSlots. Determines which slot is the target and only adds that
     * item to the output.
     * @param output - (from vanilla) output compound
     * @param nbt - nbt path
     */
    void bellows_storeAsSlotsFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);
}
