package net.smithed.bellows.mixin.nbt.inventory;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import net.smithed.bellows.mixin_interface.nbt.EnderChestInventoryExtender;
import net.smithed.bellows.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEnderChestContainer.class)
public abstract class PlayerEnderChestContainerMixin extends SimpleContainer implements EnderChestInventoryExtender {

    @Shadow
    public abstract void storeAsSlots(final ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output);

    /**
     * {@inheritDoc}
     */
    @Override
    public void bellows_storeAsSlotsFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt) {
        int slot = NBTUtils.getSlot(nbt);
        if(slot >= 0 && slot <= 26) {
            ItemStack itemStack = this.getItem(slot);
            if (!itemStack.isEmpty()) {
                output.add(new ItemStackWithSlot(slot, itemStack));
            }
        } else {
            this.storeAsSlots(output);
        }
    }
}
