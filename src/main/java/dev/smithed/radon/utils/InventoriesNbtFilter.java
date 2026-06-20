package dev.smithed.radon.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class InventoriesNbtFilter {

    public static CompoundTag writeFilteredNbt(CompoundTag nbt, NonNullList<ItemStack> stacks, String nbtPath, HolderLookup.Provider registries) {
        int slot = dev.smithed.radon.utils.NBTUtils.getSlot(nbtPath);

        if (slot == -1) {
            ContainerHelper.saveAllItems(nbt, stacks, true, registries);
        } else {
            ListTag nbtList = new ListTag();

            ItemStack itemStack = stacks.get(slot);
            if (!itemStack.isEmpty()) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) slot);
                nbtList.add(itemStack.save(registries, nbtCompound));
            }

            nbt.put("Items", nbtList);
        }
        return nbt;
    }


    public static CompoundTag readFilteredNbt(CompoundTag nbt, NonNullList<ItemStack> stacks, String nbtPath, HolderLookup.Provider registries) {
        int slot = NBTUtils.getSlot(nbtPath);

        if (slot == -1) {
            return null;
        } else {
            ListTag nbtList = nbt.getList("Items", 10);

            for (int i = 0; i < nbtList.size(); ++i) {
                CompoundTag nbtCompound = nbtList.getCompound(i);
                int j = nbtCompound.getByte("Slot") & 255;
                if (j < stacks.size()) {
                    ItemStack.parse(registries, nbtCompound).ifPresent(
                            itemStack -> stacks.set(j, itemStack)
                    );
                }
            }

            if (nbtList.isEmpty()) {
                stacks.set(slot, ItemStack.EMPTY);
            }
        }
        return nbt;
    }

}
