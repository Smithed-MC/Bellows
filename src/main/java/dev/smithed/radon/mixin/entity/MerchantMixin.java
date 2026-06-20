package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(AbstractVillager.class)
public abstract class MerchantMixin extends PassiveMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow MerchantOffers offers;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        AbstractVillager entity = ((AbstractVillager)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Offers" -> {
                MerchantOffers tradeOfferList = entity.getOffers();
                if (!tradeOfferList.isEmpty()) {
                    nbt.put("Offers", MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), tradeOfferList).getOrThrow());
                }
            }
            case "Inventory" -> entity.writeInventoryToTag(nbt, this.registryAccess());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        AbstractVillager entity = ((AbstractVillager)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Offers" -> {
                if (nbt.contains("Offers")) {
                    DataResult<MerchantOffers> var10000 = MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), nbt.get("Offers"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.prefix("Failed to load offers: ", var10002::warn)).ifPresent((offers) -> {
                        this.offers = offers;
                    });
                }
            }
            case "Inventory" -> entity.readInventoryFromTag(nbt, this.registryAccess());
            default -> {
                return false;
            }
        }
        return true;
    }
}
