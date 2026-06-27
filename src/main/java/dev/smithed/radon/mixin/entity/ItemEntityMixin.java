package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements ICustomNBTMixin {

    @Shadow private int age;
    @Shadow private int pickupDelay;
    @Shadow private int health;
    @Shadow private EntityReference<@NotNull Entity> thrower;
    @Shadow private UUID target;

    @Override
    public boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.radon_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        ItemEntity entity = ((ItemEntity)(Object)this);

        switch (topLevelNbt) {
            case "Health" -> output.putShort("Health", (short)this.health);
            case "Age" -> output.putShort("Age", (short)this.age);
            case "PickupDelay" -> output.putShort("PickupDelay", (short)this.pickupDelay);
            case "Thrower" -> EntityReference.store(this.thrower, output, "Thrower");
            case "Owner" -> output.storeNullable("Owner", UUIDUtil.CODEC, this.target);
            case "Item" -> {
                if (!entity.getItem().isEmpty()) {
                    output.store("Item", ItemStack.CODEC, entity.getItem());
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean radon_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.radon_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        ItemEntity entity = ((ItemEntity)(Object)this);

        switch (topLevelNbt) {
            case "Health" -> this.health = input.getShortOr("Health", (short)5);
            case "Age" -> input.getShortOr("Age", (short)0);
            case "PickupDelay" -> input.getShortOr("PickupDelay", (short)0);
            case "Owner" -> this.target = input.read("Owner", UUIDUtil.CODEC).orElse(null);
            case "Thrower" -> EntityReference.read(input, "Thrower");
            case "Item" -> entity.setItem(input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
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
