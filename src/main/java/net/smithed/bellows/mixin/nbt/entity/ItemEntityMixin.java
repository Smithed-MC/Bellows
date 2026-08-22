package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.smithed.bellows.mixin_interface.nbt.FilteredNbtAccessExtender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends EntityMixin implements FilteredNbtAccessExtender {

    @Shadow
    private int age;
    @Shadow
    private int pickupDelay;
    @Shadow
    private int health;
    @Shadow
    private EntityReference<@NotNull Entity> thrower;
    @Shadow
    private UUID target;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        ItemEntity entity = ((ItemEntity)(Object)this);

        switch (topLevelNbt) {
            case "Health" -> this.health = input.getShortOr("Health", (short)5);
            case "Age" -> this.age = input.getShortOr("Age", (short)0);
            case "PickupDelay" -> this.pickupDelay = input.getShortOr("PickupDelay", (short)0);
            case "Owner" -> this.target = input.read("Owner", UUIDUtil.CODEC).orElse(null);
            case "Thrower" -> this.thrower = EntityReference.read(input, "Thrower");
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
