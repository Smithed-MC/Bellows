package dev.smithed.radon.mixin.misc;

import dev.smithed.radon.mixin_interface.IPlayerInventoryExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements IPlayerInventoryExtender {

    @Shadow DefaultedList<ItemStack> main;
    @Shadow DefaultedList<ItemStack> armor;
    @Shadow DefaultedList<ItemStack> offHand;
    @Shadow @Final PlayerEntity player;

    @Shadow
    public abstract NbtList writeNbt(NbtList nbtList);

    @Override
    public NbtList writeNbtFiltered(NbtList nbtList, String nbt) {
        int slot = NBTUtils.getSlot(nbt);
        if(slot >= 0 && slot <= 35) {
            if (!(this.main.get(slot)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) slot);
                nbtList.add(this.main.get(slot).toNbt(this.player.getRegistryManager(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else if(slot >= 100 && slot <= 103) {
            if (!(this.armor.get(slot-100)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) slot);
                nbtList.add(this.armor.get(slot-100).toNbt(this.player.getRegistryManager(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else if(slot == -106) {
            if (!(this.offHand.get(0)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) -106);
                nbtList.add(this.offHand.get(0).toNbt(this.player.getRegistryManager(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else {
            this.writeNbt(nbtList);
        }
        return nbtList;
    }

}
