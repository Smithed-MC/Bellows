package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ICustomNBTMixin {

    @Shadow @Final private ServerRecipeBook recipeBook;
    @Shadow @Final private Set<ThrownEnderpearl> enderPearls;
    @Shadow private Vec3 enteredNetherPosition;
    @Shadow private WardenSpawnTracker wardenSpawnTracker;
    @Shadow private boolean spawnExtraParticlesOnFall;
    @Nullable @Shadow private BlockPos raidOmenPosition;
    @Nullable @Shadow private Vec3 currentExplosionImpactPos;
    @Shadow protected abstract void storeGameTypes(final ValueOutput playerOutput);
    @Shadow protected abstract void saveParentVehicle(final ValueOutput playerOutput);
    @Shadow protected abstract void saveEnderPearls(final ValueOutput playerOutput);

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        ServerPlayer entity = ((ServerPlayer) (Object) this);

        switch (topLevelNbt) {
            case "warden_spawn_tracker" -> output.store("warden_spawn_tracker", WardenSpawnTracker.CODEC, this.wardenSpawnTracker);
            case "playerGameType", "previousPlayerGameType" -> this.storeGameTypes(output);
            case "seenCredits" -> output.putBoolean("seenCredits", entity.seenCredits);
            case "entered_nether_pos" -> output.storeNullable("entered_nether_pos", Vec3.CODEC, this.enteredNetherPosition);
            case "last_explosion_impact_pos" -> output.storeNullable("last_explosion_impact_pos", Vec3.CODEC, this.currentExplosionImpactPos);
            case "RootVehicle" -> this.saveParentVehicle(output);
            case "recipeBook" -> output.store("recipeBook", ServerRecipeBook.Packed.CODEC, this.recipeBook.pack());
            case "Dimension" -> output.putString("Dimension", entity.level().dimension().identifier().toString());
            case "respawn" -> output.storeNullable("respawn", ServerPlayer.RespawnConfig.CODEC, entity.getRespawnConfig());
            case "spawn_extra_particles_on_fall" -> output.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
            case "raid_omen_position" -> output.storeNullable("raid_omen_position", BlockPos.CODEC, this.raidOmenPosition);
            case "ender_pearls" -> this.saveEnderPearls(output);
            case "ShoulderEntityLeft" -> {
                if (!entity.getShoulderEntityLeft().isEmpty()) {
                    output.store("ShoulderEntityLeft", CompoundTag.CODEC, entity.getShoulderEntityLeft());
                }
            }
            case "ShoulderEntityRight" -> {
                if (!entity.getShoulderEntityRight().isEmpty()) {
                    output.store("ShoulderEntityRight", CompoundTag.CODEC, entity.getShoulderEntityRight());
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
