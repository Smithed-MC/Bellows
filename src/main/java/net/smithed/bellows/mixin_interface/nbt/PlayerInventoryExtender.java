package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface PlayerInventoryExtender {

    void bellows_saveWithoutIdFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);

}
