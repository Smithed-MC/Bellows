package dev.smithed.radon.mixin_interface;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public interface IPlayerInventoryExtender {

    void radon_saveWithoutIdFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt);

}
