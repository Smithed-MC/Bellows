package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin extends PassiveEntityMixin implements ICustomNBTMixin {

    @Shadow SimpleInventory inventory;
    @Shadow TradeOfferList offers;



    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MerchantEntity entity = ((MerchantEntity)(Object)this);
        if(!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries)) {
            switch (topLevelNbt) {
                case "Offers" -> {
                    TradeOfferList tradeOfferList = entity.getOffers();
                    if (!tradeOfferList.isEmpty()) {
                        nbt.put("Offers", (NbtElement)TradeOfferList.CODEC.encodeStart(this.getRegistryManager().getOps(NbtOps.INSTANCE), tradeOfferList).getOrThrow());
                    }
                }
                case "Inventory" -> nbt.put("Inventory", this.inventory.toNbtList(registries));
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MerchantEntity entity = ((MerchantEntity)(Object)this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {

            switch (topLevelNbt) {
                case "Offers" -> {
                    if (nbt.contains("Offers", 10)) {
                        var result = TradeOfferList.CODEC.parse(this.getRegistryManager().getOps(NbtOps.INSTANCE), nbt.get("Offers"));

                        result.ifSuccess(offers -> this.offers = offers);
                    }

                }
                case "Inventory" -> this.inventory.readNbtList(nbt.getList("Inventory", 10), registries);
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
