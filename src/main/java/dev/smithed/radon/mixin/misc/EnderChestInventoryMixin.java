package dev.smithed.radon.mixin.misc;

import dev.smithed.radon.mixin_interface.IEnderChestInventoryExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEnderChestContainer.class)
public abstract class EnderChestInventoryMixin extends SimpleContainer implements IEnderChestInventoryExtender {

    @Shadow
    public abstract ListTag createTag(HolderLookup.Provider registries);

    @Override
    public ListTag toNbtListFiltered(String nbt, HolderLookup.Provider registries) {
        ListTag nbtList = new ListTag();

        int slot = NBTUtils.getSlot(nbt);
        if(slot >= 0 && slot <= 26) {
            ItemStack itemStack = this.getItem(slot);
            if (!itemStack.isEmpty()) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)slot);
                itemStack.save(registries, nbtCompound);
                nbtList.add(itemStack.save(registries, nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else {
            nbtList = this.createTag(registries);
        }
        return nbtList;
    }
}
