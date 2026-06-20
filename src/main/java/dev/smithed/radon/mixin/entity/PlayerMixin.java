package dev.smithed.radon.mixin.entity;

import com.mojang.serialization.DataResult;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEnderChestInventoryExtender;
import dev.smithed.radon.mixin_interface.IPlayerInventoryExtender;
import net.minecraft.SharedConstants;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow int sleepCounter;
    @Shadow int enchantmentSeed;
    @Shadow Inventory inventory;
    @Shadow FoodData foodData;
    @Shadow Abilities abilities;
    @Shadow PlayerEnderChestContainer enderChestInventory;
    @Nullable @Shadow Vec3 currentImpulseImpactPos;
    @Shadow boolean ignoreFallDamageFromCurrentImpulse;
    @Shadow int currentImpulseContextResetGraceTime;

    @Shadow abstract void setShoulderEntityRight(CompoundTag entityNbt);
    @Shadow abstract void setShoulderEntityLeft(CompoundTag entityNbt);

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Player entity = ((Player) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "DataVersion" ->
                    nbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            case "Inventory" -> {
                if (this.inventory instanceof IPlayerInventoryExtender mixin)
                    nbt.put("Inventory", mixin.saveWithoutIdFiltered(new ListTag(), path));
                else
                    nbt.put("Inventory", this.inventory.save(new ListTag()));
            }
            case "SelectedItemSlot" -> nbt.putInt("SelectedItemSlot", this.inventory.selected);
            case "SleepTimer" -> nbt.putShort("SleepTimer", (short) this.sleepCounter);
            case "XpP" -> nbt.putFloat("XpP", entity.experienceProgress);
            case "XpLevel" -> nbt.putInt("XpLevel", entity.experienceLevel);
            case "XpTotal" -> nbt.putInt("XpTotal", entity.totalExperience);
            case "XpSeed" -> nbt.putInt("XpSeed", this.enchantmentSeed);
            case "Score" -> nbt.putInt("Score", entity.getScore());
            case "foodLevel", "foodTickTimer", "foodSaturationLevel", "foodExhaustionLevel" ->
                    this.foodData.addAdditionalSaveData(nbt);
            case "abilities" -> this.abilities.addSaveData(nbt);
            case "EnderItems" -> {
                if (this.enderChestInventory instanceof IEnderChestInventoryExtender mixin)
                    nbt.put("EnderItems", mixin.toNbtListFiltered(path, this.registryAccess()));
                else
                    nbt.put("EnderItems", this.enderChestInventory.createTag(this.registryAccess()));
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
            case "LastDeathLocation" -> entity.getLastDeathLocation().flatMap((globalPos) -> {
                DataResult<Tag> var10002 = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos);
                Logger var10003 = Radon.LOGGER;
                Objects.requireNonNull(var10003);
                return var10002.resultOrPartial(var10003::error);
            }).ifPresent((nbtElement) -> {
                nbt.put("LastDeathLocation", nbtElement);
            });
            case "current_explosion_impact_pos" -> {
                if (this.currentImpulseImpactPos != null) {
                    nbt.put("current_explosion_impact_pos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.currentImpulseImpactPos).getOrThrow());
                }
            }
            case "ignore_fall_damage_from_current_explosion" -> {
                nbt.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentImpulse);
            }
            case "current_impulse_context_reset_grace_time" -> {
                nbt.putInt("current_impulse_context_reset_grace_time", this.currentImpulseContextResetGraceTime);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
