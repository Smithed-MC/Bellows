package net.smithed.bellows.mixin.nbt.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import net.smithed.bellows.mixin_interface.nbt.PlayerInventoryExtender;
import net.smithed.bellows.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements PlayerInventoryExtender {

    @Final @Shadow private NonNullList<@NotNull ItemStack> items;
    @Shadow public abstract void save(final ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output);

    @Override
    public void bellows_saveWithoutIdFiltered(ValueOutput.TypedOutputList<@NotNull ItemStackWithSlot> output, String nbt) {
        int slot = NBTUtils.getSlot(nbt);
        if( (slot >= 0 && slot <= 35) || (slot >= 100 && slot <= 103) || (slot == -106)) {
            ItemStack item = this.items.get(slot);
            if (!item.isEmpty()) {
                output.add(new ItemStackWithSlot(slot, item));
            }
        } else {
            this.save(output);
        }
    }
}
