package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Villager.class)
public abstract class VillagerMixin extends MerchantMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow int foodLevel;
    @Shadow GossipContainer gossips;
    @Shadow long lastRestockGameTime;
    @Shadow int numberOfRestocksToday;
    @Shadow int villagerXp;
    @Shadow boolean assignProfessionWhenSpawned;
    @Shadow long lastGossipDecayTime;
    @Shadow @Final static EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Villager entity = ((Villager)(Object)this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "FoodLevel" -> nbt.putByte("FoodLevel", (byte) this.foodLevel);
            case "Gossips" -> nbt.put("Gossips", this.gossips.store(NbtOps.INSTANCE));
            case "Xp" -> nbt.putInt("Xp", this.villagerXp);
            case "LastRestock" -> nbt.putLong("LastRestock", this.lastRestockGameTime);
            case "LastGossipDecay" -> nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
            case "RestocksToday" -> nbt.putInt("RestocksToday", this.numberOfRestocksToday);
            case "AssignProfessionWhenSpawned" -> {
                if (this.assignProfessionWhenSpawned) {
                    nbt.putBoolean("AssignProfessionWhenSpawned", true);
                }
            }
            case "VillagerData" -> {
                DataResult<Tag> var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, entity.getVillagerData());
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
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Villager entity = ((Villager)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "Offers" -> {
                if (nbt.contains("Offers", 10)) {
                    var result = MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), nbt.get("Offers"));

                    result.ifSuccess(offers -> this.offers = offers);
                }
            }
            case "FoodLevel" -> {
                if (nbt.contains("FoodLevel", 1))
                    this.foodLevel = nbt.getByte("FoodLevel");
            }
            case "Gossips" -> {
                ListTag nbtList = nbt.getList("Gossips", 10);
                this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, nbtList));
            }
            case "Xp" -> {
                if (nbt.contains("Xp", 3))
                    this.villagerXp = nbt.getInt("Xp");
            }
            case "LastRestock" -> this.lastRestockGameTime = nbt.getLong("LastRestock");
            case "LastGossipDecay" -> this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");
            case "RestocksToday" -> this.numberOfRestocksToday = nbt.getInt("RestocksToday");
            case "AssignProfessionWhenSpawned" -> this.assignProfessionWhenSpawned = nbt.getBoolean("AssignProfessionWhenSpawned");
            case "VillagerData" -> {
                if (nbt.contains("VillagerData", 10)) {
                    DataResult<VillagerData> var10000 = VillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("VillagerData"));
                    Logger var10001 = LOGGER;
                    Objects.requireNonNull(var10001);
                    var10000.resultOrPartial(var10001::error).ifPresent((villagerData) -> {
                        this.entityData.set(DATA_VILLAGER_DATA, villagerData);
                    });
                }
            }
            default -> {
                return false;
            }
        }
        if (this.level instanceof ServerLevel)
            entity.refreshBrain((ServerLevel)this.level);
        entity.setCanPickUpLoot(true);
        return true;
    }
}
