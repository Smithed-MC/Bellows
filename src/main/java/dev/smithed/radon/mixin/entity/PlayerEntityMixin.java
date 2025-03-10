package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEnderChestInventoryExtender;
import dev.smithed.radon.mixin_interface.IPlayerInventoryExtender;
import net.minecraft.SharedConstants;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow int sleepTimer;
    @Shadow int enchantingTableSeed;
    @Shadow PlayerInventory inventory;
    @Shadow HungerManager hungerManager;
    @Shadow PlayerAbilities abilities;
    @Shadow EnderChestInventory enderChestInventory;
    @Nullable @Shadow Vec3d currentExplosionImpactPos;
    @Shadow boolean ignoreFallDamageFromCurrentExplosion;
    @Shadow int currentExplosionResetGraceTime;

    @Shadow abstract void setShoulderEntityRight(NbtCompound entityNbt);
    @Shadow abstract void setShoulderEntityLeft(NbtCompound entityNbt);

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        PlayerEntity entity = ((PlayerEntity) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DataVersion" ->
                    nbt.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
            case "Inventory" -> {
                if (this.inventory instanceof IPlayerInventoryExtender mixin)
                    nbt.put("Inventory", mixin.writeNbtFiltered(new NbtList(), path));
                else
                    nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
            }
            case "SelectedItemSlot" -> nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
            case "SleepTimer" -> nbt.putShort("SleepTimer", (short) this.sleepTimer);
            case "XpP" -> nbt.putFloat("XpP", entity.experienceProgress);
            case "XpLevel" -> nbt.putInt("XpLevel", entity.experienceLevel);
            case "XpTotal" -> nbt.putInt("XpTotal", entity.totalExperience);
            case "XpSeed" -> nbt.putInt("XpSeed", this.enchantingTableSeed);
            case "Score" -> nbt.putInt("Score", entity.getScore());
            case "foodLevel", "foodTickTimer", "foodSaturationLevel", "foodExhaustionLevel" ->
                    this.hungerManager.writeNbt(nbt);
            case "abilities" -> this.abilities.writeNbt(nbt);
            case "EnderItems" -> {
                if (this.enderChestInventory instanceof IEnderChestInventoryExtender mixin)
                    nbt.put("EnderItems", mixin.toNbtListFiltered(path, this.getRegistryManager()));
                else
                    nbt.put("EnderItems", this.enderChestInventory.toNbtList(this.getRegistryManager()));
            }
            case "ShoulderEntityLeft" -> {
                if (!entity.getShoulderEntityLeft().isEmpty()) {
                    nbt.put("ShoulderEntityLeft", entity.getShoulderEntityLeft());
                }
            }
            case "ShoulderEntityRight" -> {
                if (!entity.getShoulderEntityRight().isEmpty()) {
                    nbt.put("ShoulderEntityRight", entity.getShoulderEntityRight());
                }
            }
            case "LastDeathLocation" -> entity.getLastDeathPos().flatMap((globalPos) -> {
                DataResult<NbtElement> var10002 = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos);
                Logger var10003 = Radon.LOGGER;
                Objects.requireNonNull(var10003);
                return var10002.resultOrPartial(var10003::error);
            }).ifPresent((nbtElement) -> {
                nbt.put("LastDeathLocation", nbtElement);
            });
            case "current_explosion_impact_pos" -> {
                if (this.currentExplosionImpactPos != null) {
                    nbt.put("current_explosion_impact_pos", Vec3d.CODEC.encodeStart(NbtOps.INSTANCE, this.currentExplosionImpactPos).getOrThrow());
                }
            }
            case "ignore_fall_damage_from_current_explosion" -> {
                nbt.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentExplosion);
            }
            case "current_impulse_context_reset_grace_time" -> {
                nbt.putInt("current_impulse_context_reset_grace_time", this.currentExplosionResetGraceTime);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
