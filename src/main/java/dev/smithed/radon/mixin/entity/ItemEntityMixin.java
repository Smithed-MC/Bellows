package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow int itemAge;
    @Shadow int pickupDelay;
    @Shadow int health;
    @Shadow UUID throwerUuid;
    @Shadow UUID owner;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        ItemEntity entity = ((ItemEntity)(Object)this);

        switch (topLevelNbt) {
            case "Health" -> nbt.putShort("Health", (short)this.health);
            case "Age" -> nbt.putShort("Age", (short)this.itemAge);
            case "PickupDelay" -> nbt.putShort("PickupDelay", (short)this.pickupDelay);
            case "Thrower" -> {
                if (this.throwerUuid != null) {
                    nbt.putUuid("Thrower", this.throwerUuid);
                }
            }
            case "Owner" -> {
                if (entity.getOwner() != null) {
                    nbt.putUuid("Owner", this.owner);
                }
            }
            case "Item" -> {
                if (!entity.getStack().isEmpty()) {
                    nbt.put("Item", entity.getStack().toNbt(registries, new NbtCompound()));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        ItemEntity entity = ((ItemEntity)(Object)this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {

            switch (topLevelNbt) {
                case "Health" -> this.health = nbt.getShort("Health");
                case "Age" -> this.itemAge = nbt.getShort("Age");
                case "PickupDelay" -> this.pickupDelay = nbt.getShort("PickupDelay");
                case "Owner" -> this.owner = nbt.getUuid("Owner");
                case "Thrower" -> this.throwerUuid = nbt.getUuid("Thrower");
                case "Item" -> {
                    NbtCompound nbtCompound = nbt.getCompound("Item");
                    ItemStack.fromNbt(registries, nbtCompound).ifPresent(entity::setStack);
                    if (entity.getStack().isEmpty()) {
                        entity.discard();
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
