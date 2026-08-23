package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface PlayerInventoryExtender {

    /**
     * Bypass of PlayerInventory::save, saves a specific item slot instead of the entire inventory.
     * @param output - (from vanilla)
     * @param nbt - nbt path to get slot from
     */
    void bellows_saveFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);
}
