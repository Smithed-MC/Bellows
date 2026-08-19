package dev.smithed.radon.mixin.entity;

import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntityMixin {

    @Shadow BlockPos wanderTarget;
    @Shadow int despawnDelay;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DespawnDelay" -> nbt.putInt("DespawnDelay", this.despawnDelay);
            case "wander_target" -> {
                if (this.wanderTarget != null) {
                    nbt.put("wander_target", NbtHelper.fromBlockPos(this.wanderTarget));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        WanderingTraderEntity entity = ((WanderingTraderEntity)(Object)this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DespawnDelay" -> {
                if (nbt.contains("DespawnDelay", 99))
                    this.despawnDelay = nbt.getInt("DespawnDelay");
            }
            case "wander_target" -> NbtHelper.toBlockPos(nbt, "wander_target").ifPresent((wanderTarget) -> this.wanderTarget = wanderTarget);
            default -> {
                return false;
            }
        }
        entity.setBreedingAge(Math.max(0, entity.getBreedingAge()));
        return true;
    }
}
