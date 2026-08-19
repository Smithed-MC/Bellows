package dev.smithed.radon.mixin.misc;

import dev.smithed.radon.mixin_interface.IEnderChestInventoryExtender;
import dev.smithed.radon.mixin_interface.IPlayerInventoryExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnderChestInventory.class)
public abstract class EnderChestInventoryMixin extends SimpleInventory implements IEnderChestInventoryExtender {

    @Shadow
    public abstract NbtList toNbtList(RegistryWrapper.WrapperLookup registries);

    @Override
    public NbtList toNbtListFiltered(String nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        int slot = NBTUtils.getSlot(nbt);
        if(slot >= 0 && slot <= 26) {
            ItemStack itemStack = this.getStack(slot);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte)slot);
                itemStack.toNbt(registries, nbtCompound);
                nbtList.add(itemStack.toNbt(registries, nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else {
            nbtList = this.toNbtList(registries);
        }
        return nbtList;
    }
}
