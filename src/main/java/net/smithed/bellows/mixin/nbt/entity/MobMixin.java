package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.smithed.bellows.mixin_interface.nbt.FilteredNbtAccessExtender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntityMixin implements FilteredNbtAccessExtender {

    @Shadow
    private boolean persistenceRequired;
    @Shadow
    private long lootTableSeed;
    @Shadow
    private DropChances dropChances;
    @Shadow
    private Optional<ResourceKey<LootTable>> lootTable;
    @Shadow
    private BlockPos homePosition;
    @Shadow
    private int homeRadius;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Mob entity = ((Mob) (Object) this);

        switch (topLevelNbt) {
            case "CanPickUpLoot" -> entity.setCanPickUpLoot(input.getBooleanOr("CanPickUpLoot", false));
            case "PersistenceRequired" -> this.persistenceRequired = input.getBooleanOr("PersistenceRequired", false);
            case "drop_chances" -> this.dropChances = input.read("drop_chances", DropChances.CODEC).orElse(DropChances.DEFAULT);
            case "leash" -> entity.readLeashData(input);
            case "LeftHanded" -> entity.setLeftHanded(input.getBooleanOr("LeftHanded", false));
            case "home_radius" -> {
                this.homeRadius = input.getIntOr("home_radius", -1);
                if (this.homeRadius >= 0) {
                    this.homePosition = input.read("home_pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
                }
            }
            case "home_pos" -> {} // handled by 'home_radius'
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
