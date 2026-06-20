package dev.smithed.radon.mixin.misc;

import dev.smithed.radon.mixin_interface.IPlayerInventoryExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements IPlayerInventoryExtender {

    @Shadow NonNullList<ItemStack> items;
    @Shadow NonNullList<ItemStack> armor;
    @Shadow NonNullList<ItemStack> offhand;
    @Shadow @Final Player player;

    @Shadow
    public abstract ListTag save(ListTag nbtList);

    @Override
    public ListTag saveWithoutIdFiltered(ListTag nbtList, String nbt) {
        int slot = NBTUtils.getSlot(nbt);
        if(slot >= 0 && slot <= 35) {
            if (!(this.items.get(slot)).isEmpty()) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) slot);
                nbtList.add(this.items.get(slot).save(this.player.registryAccess(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else if(slot >= 100 && slot <= 103) {
            if (!(this.armor.get(slot-100)).isEmpty()) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) slot);
                nbtList.add(this.armor.get(slot-100).save(this.player.registryAccess(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else if(slot == -106) {
            if (!(this.offhand.get(0)).isEmpty()) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) -106);
                nbtList.add(this.offhand.get(0).save(this.player.registryAccess(), nbtCompound));
                nbtList.add(nbtCompound);
            }
        } else {
            this.save(nbtList);
        }
        return nbtList;
    }

}
