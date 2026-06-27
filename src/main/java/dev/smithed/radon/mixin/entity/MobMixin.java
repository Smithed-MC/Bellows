package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow private boolean persistenceRequired;
    @Shadow private long lootTableSeed;
    @Shadow private DropChances dropChances;
    @Shadow private Optional<ResourceKey<LootTable>> lootTable;

    @Override
    public boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.radon_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Mob entity = ((Mob) (Object) this);

        switch (topLevelNbt) {
            case "CanPickUpLoot" -> output.putBoolean("CanPickUpLoot", entity.canPickUpLoot());
            case "PersistenceRequired" -> output.putBoolean("PersistenceRequired", this.persistenceRequired);
            case "drop_chances" -> {
                if (!this.dropChances.equals(DropChances.DEFAULT)) {
                    output.store("drop_chances", DropChances.CODEC, this.dropChances);
                }
            }
            case "leash" -> entity.writeLeashData(output, entity.getLeashData());
            case "home_radius", "home_pos" -> {
                if (entity.hasHome()) {
                    output.putInt("home_radius", entity.getHomeRadius());
                    output.store("home_pos", BlockPos.CODEC, entity.getHomePosition());
                }
            }
            case "LeftHanded" -> output.putBoolean("LeftHanded", entity.isLeftHanded());
            case "DeathLootTable" -> this.lootTable.ifPresent((lootTable) -> output.store("DeathLootTable", LootTable.KEY_CODEC, lootTable));
            case "DeathLootTableSeed" -> {
                if (this.lootTableSeed != 0L) {
                    output.putLong("DeathLootTableSeed", this.lootTableSeed);
                }
            }
            case "NoAI" -> {
                if (entity.isNoAi()) {
                    output.putBoolean("NoAI", entity.isNoAi());
                }
            }
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
        Mob entity = ((Mob) (Object) this);

        switch (topLevelNbt) {
            case "CanPickUpLoot" -> entity.setCanPickUpLoot(input.getBooleanOr("CanPickUpLoot", false));
            case "PersistenceRequired" -> this.persistenceRequired = input.getBooleanOr("PersistenceRequired", false);
            case "drop_chances" -> this.dropChances = input.read("drop_chances", DropChances.CODEC).orElse(DropChances.DEFAULT);
            case "leash" -> entity.readLeashData(input);
            case "LeftHanded" -> entity.setLeftHanded(input.getBooleanOr("LeftHanded", false));
            case "DeathLootTable" -> this.lootTable = input.read("DeathLootTable", LootTable.KEY_CODEC);
            case "DeathLootTableSeed" -> this.lootTableSeed = input.getLongOr("DeathLootTableSeed", 0L);
            case "NoAI" -> entity.setNoAi(input.getBooleanOr("NoAI", false));
            default -> {
                return false;
            }
        }
        return true;
    }
}
