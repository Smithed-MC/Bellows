package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import net.minecraft.village.TradeOfferList;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin extends PassiveEntityMixin implements ICustomNBTMixin {

    @Shadow TradeOfferList offers;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        MerchantEntity entity = ((MerchantEntity)(Object)this);
        if(!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            switch (topLevelNbt) {
                case "Offers" -> {
                    TradeOfferList tradeOfferList = entity.getOffers();
                    if (!tradeOfferList.isEmpty()) {
                        nbt.put("Offers", TradeOfferList.CODEC.encodeStart(this.getRegistryManager().getOps(NbtOps.INSTANCE), tradeOfferList).getOrThrow());
                    }
                }
                case "Inventory" -> entity.writeInventory(nbt, this.getRegistryManager());
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        MerchantEntity entity = ((MerchantEntity)(Object)this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {

            switch (topLevelNbt) {
                case "Offers" -> {
                    if (nbt.contains("Offers")) {
                        DataResult<TradeOfferList> var10000 = TradeOfferList.CODEC.parse(this.getRegistryManager().getOps(NbtOps.INSTANCE), nbt.get("Offers"));
                        Logger var10002 = LOGGER;
                        Objects.requireNonNull(var10002);
                        var10000.resultOrPartial(Util.addPrefix("Failed to load offers: ", var10002::warn)).ifPresent((offers) -> {
                            this.offers = offers;
                        });
                    }
                }
                case "Inventory" -> entity.readInventory(nbt, this.getRegistryManager());
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
