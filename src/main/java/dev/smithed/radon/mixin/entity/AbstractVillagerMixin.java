package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin extends AgeableMobMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow protected MerchantOffers offers;

    @Override
    public boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.radon_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        AbstractVillager entity = ((AbstractVillager)(Object)this);

        switch (topLevelNbt) {
            case "Offers" -> {
                if (!entity.level().isClientSide() && this.offers != null) {
                    output.store("Offers", MerchantOffers.CODEC, this.offers);
                }
            }
            case "Inventory" -> entity.writeInventoryToTag(output);
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
        AbstractVillager entity = ((AbstractVillager)(Object)this);

        switch (topLevelNbt) {
            case "Offers" -> this.offers = input.read("Offers", MerchantOffers.CODEC).orElse(null);
            case "Inventory" -> entity.readInventoryFromTag(input);
            default -> {
                return false;
            }
        }
        return true;
    }
}
