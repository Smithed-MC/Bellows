package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerGossips;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntityMixin {

    @Shadow int foodLevel;
    @Shadow VillagerGossips gossip;
    @Shadow long lastRestockTime;
    @Shadow int restocksToday;
    @Shadow int experience;
    @Shadow boolean natural;
    @Shadow long lastGossipDecayTime;
    @Shadow @Final static TrackedData<VillagerData> VILLAGER_DATA;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        VillagerEntity entity = ((VillagerEntity)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "FoodLevel" -> nbt.putByte("FoodLevel", (byte) this.foodLevel);
            case "Gossips" -> nbt.put("Gossips", this.gossip.serialize(NbtOps.INSTANCE));
            case "Xp" -> nbt.putInt("Xp", this.experience);
            case "LastRestock" -> nbt.putLong("LastRestock", this.lastRestockTime);
            case "LastGossipDecay" -> nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
            case "RestocksToday" -> nbt.putInt("RestocksToday", this.restocksToday);
            case "AssignProfessionWhenSpawned" -> {
                if (this.natural) {
                    nbt.putBoolean("AssignProfessionWhenSpawned", true);
                }
            }
            case "VillagerData" -> {
                DataResult<NbtElement> var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, entity.getVillagerData());
                Logger var10001 = LOGGER;
                Objects.requireNonNull(var10001);
                var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> {
                    nbt.put("VillagerData", nbtElement);
                });
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        VillagerEntity entity = ((VillagerEntity)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Offers" -> {
                if (nbt.contains("Offers", 10)) {
                    var result = TradeOfferList.CODEC.parse(this.getRegistryManager().getOps(NbtOps.INSTANCE), nbt.get("Offers"));

                    result.ifSuccess(offers -> this.offers = offers);
                }
            }
            case "FoodLevel" -> {
                if (nbt.contains("FoodLevel", 1))
                    this.foodLevel = nbt.getByte("FoodLevel");
            }
            case "Gossips" -> {
                NbtList nbtList = nbt.getList("Gossips", 10);
                this.gossip.deserialize(new Dynamic<>(NbtOps.INSTANCE, nbtList));
            }
            case "Xp" -> {
                if (nbt.contains("Xp", 3))
                    this.experience = nbt.getInt("Xp");
            }
            case "LastRestock" -> this.lastRestockTime = nbt.getLong("LastRestock");
            case "LastGossipDecay" -> this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");
            case "RestocksToday" -> this.restocksToday = nbt.getInt("RestocksToday");
            case "AssignProfessionWhenSpawned" -> this.natural = nbt.getBoolean("AssignProfessionWhenSpawned");
            case "VillagerData" -> {
                if (nbt.contains("VillagerData", 10)) {
                    DataResult<VillagerData> var10000 = VillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("VillagerData"));
                    Logger var10001 = LOGGER;
                    Objects.requireNonNull(var10001);
                    var10000.resultOrPartial(var10001::error).ifPresent((villagerData) -> {
                        this.dataTracker.set(VILLAGER_DATA, villagerData);
                    });
                }
            }
            default -> {
                return false;
            }
        }
        if (this.world instanceof ServerWorld)
            entity.reinitializeBrain((ServerWorld)this.world);
        entity.setCanPickUpLoot(true);
        return true;
    }
}
