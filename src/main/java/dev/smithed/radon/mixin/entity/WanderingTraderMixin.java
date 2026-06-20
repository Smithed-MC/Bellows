package dev.smithed.radon.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends MerchantMixin {

    @Shadow BlockPos wanderTarget;
    @Shadow int despawnDelay;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DespawnDelay" -> nbt.putInt("DespawnDelay", this.despawnDelay);
            case "wander_target" -> {
                if (this.wanderTarget != null) {
                    nbt.put("wander_target", NbtUtils.writeBlockPos(this.wanderTarget));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        WanderingTrader entity = ((WanderingTrader)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DespawnDelay" -> {
                if (nbt.contains("DespawnDelay", 99))
                    this.despawnDelay = nbt.getInt("DespawnDelay");
            }
            case "wander_target" -> NbtUtils.readBlockPos(nbt, "wander_target").ifPresent((wanderTarget) -> this.wanderTarget = wanderTarget);
            default -> {
                return false;
            }
        }
        entity.setAge(Math.max(0, entity.getAge()));
        return true;
    }
}
