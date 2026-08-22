package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillagerMixin {

    @Shadow
    private BlockPos wanderTarget;
    @Shadow
    private int despawnDelay;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }

        switch (topLevelNbt) {
            case "DespawnDelay" -> output.putInt("DespawnDelay", this.despawnDelay);
            case "wander_target" -> output.storeNullable("wander_target", BlockPos.CODEC, this.wanderTarget);
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
        WanderingTrader entity = ((WanderingTrader)(Object)this);

        switch (topLevelNbt) {
            case "DespawnDelay" -> this.despawnDelay = input.getIntOr("DespawnDelay", 0);
            case "wander_target" -> this.wanderTarget = input.read("wander_target", BlockPos.CODEC).orElse(null);
            default -> {
                return false;
            }
        }
        entity.setAge(Math.max(0, entity.getAge()));
        return true;
    }
}
