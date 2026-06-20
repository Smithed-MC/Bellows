package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow int age;
    @Shadow int pickupDelay;
    @Shadow int health;
    @Shadow UUID thrower;
    @Shadow UUID target;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        ItemEntity entity = ((ItemEntity)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Health" -> nbt.putShort("Health", (short)this.health);
            case "Age" -> nbt.putShort("Age", (short)this.age);
            case "PickupDelay" -> nbt.putShort("PickupDelay", (short)this.pickupDelay);
            case "Thrower" -> {
                if (this.thrower != null) {
                    nbt.putUUID("Thrower", this.thrower);
                }
            }
            case "Owner" -> {
                if (entity.getOwner() != null) {
                    nbt.putUUID("Owner", this.target);
                }
            }
            case "Item" -> {
                if (!entity.getItem().isEmpty()) {
                    nbt.put("Item", entity.getItem().save(this.registryAccess()));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        ItemEntity entity = ((ItemEntity)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Health" -> this.health = nbt.getShort("Health");
            case "Age" -> this.age = nbt.getShort("Age");
            case "PickupDelay" -> this.pickupDelay = nbt.getShort("PickupDelay");
            case "Owner" -> this.target = nbt.getUUID("Owner");
            case "Thrower" -> this.thrower = nbt.getUUID("Thrower");
            case "Item" -> {
                if (nbt.contains("Item", 10)) {
                    CompoundTag nbtCompound = nbt.getCompound("Item");
                    entity.setItem(ItemStack.parse(this.registryAccess(), nbtCompound).orElse(ItemStack.EMPTY));
                } else {
                    entity.setItem(ItemStack.EMPTY);
                }
            }
            default -> {
                return false;
            }
        }
        if (entity.getItem().isEmpty()) {
            entity.discard();
        }
        return true;
    }
}
