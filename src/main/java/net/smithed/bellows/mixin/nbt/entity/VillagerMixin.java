package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillagerMixin {

    @Shadow @Final
    private static EntityDataAccessor<@NotNull VillagerData> DATA_VILLAGER_DATA;
    @Shadow @Final
    private GossipContainer gossips;
    @Shadow
    private int foodLevel;
    @Shadow
    private long lastRestockGameTime;
    @Shadow
    private int numberOfRestocksToday;
    @Shadow
    private int villagerXp;
    @Shadow
    private boolean assignProfessionWhenSpawned;
    @Shadow
    private long lastGossipDecayTime;

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Villager entity = ((Villager)(Object)this);

        switch (topLevelNbt) {
            case "VillagerData", "VillagerDataFinalized" -> {
                output.store("VillagerData", VillagerData.CODEC, entity.getVillagerData());
                output.putBoolean("VillagerDataFinalized", entity.getVillagerDataFinalized());
            }
            case "FoodLevel" -> output.putByte("FoodLevel", (byte)this.foodLevel);
            case "Gossips" -> output.store("Gossips", GossipContainer.CODEC, this.gossips);
            case "Xp" -> output.putInt("Xp", this.villagerXp);
            case "LastRestock" -> output.putLong("LastRestock", this.lastRestockGameTime);
            case "LastGossipDecay" -> output.putLong("LastGossipDecay", this.lastGossipDecayTime);
            case "RestocksToday" -> output.putInt("RestocksToday", this.numberOfRestocksToday);
            case "AssignProfessionWhenSpawned" -> {
                if (this.assignProfessionWhenSpawned) {
                    output.putBoolean("AssignProfessionWhenSpawned", true);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Villager entity = ((Villager)(Object)this);

        switch (topLevelNbt) {
            case "VillagerData" -> {
                Optional<VillagerData> villagerDataOptional = input.read("VillagerData", VillagerData.CODEC);
                if (input.getBooleanOr("VillagerDataFinalized", false) || villagerDataOptional.isPresent()) {
                    entity.setVillagerDataFinalized(true);
                    VillagerData villagerData = villagerDataOptional.orElseGet(Villager::createDefaultVillagerData);
                    this.entityData.set(DATA_VILLAGER_DATA, villagerData);
                }

            }
            case "VillagerDataFinalized" -> {}
            case "FoodLevel" -> this.foodLevel = input.getByteOr("FoodLevel", (byte)0);
            case "Gossips" -> {
                this.gossips.clear();
                Optional<GossipContainer> gossip = input.read("Gossips", GossipContainer.CODEC);
                GossipContainer oldGossip = this.gossips;
                Objects.requireNonNull(oldGossip);
                gossip.ifPresent(oldGossip::putAll);
            }
            case "Xp" -> this.villagerXp = input.getIntOr("Xp", 0);
            case "LastRestock" -> this.lastRestockGameTime = input.getLongOr("LastRestock", 0L);
            case "LastGossipDecay" -> this.lastGossipDecayTime = input.getLongOr("LastGossipDecay", 0L);
            case "RestocksToday" -> this.numberOfRestocksToday = input.getIntOr("RestocksToday", 0);
            case "AssignProfessionWhenSpawned" -> this.assignProfessionWhenSpawned = input.getBooleanOr("AssignProfessionWhenSpawned", false);
            default -> {
                return false;
            }
        }
        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.refreshBrain(serverLevel);
        }
        return true;
    }
}
