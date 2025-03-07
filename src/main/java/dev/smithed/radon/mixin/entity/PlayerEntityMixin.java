package dev.smithed.radon.mixin.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IFilteredNbtList;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.GlobalPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow
    PlayerInventory inventory;
    @Shadow
    int sleepTimer;
    @Shadow
    int enchantingTableSeed;
    @Shadow
    HungerManager hungerManager;
    @Shadow
    PlayerAbilities abilities;
    @Shadow
    EnderChestInventory enderChestInventory;

    @Shadow
    abstract void setShoulderEntityRight(NbtCompound entityNbt);

    @Shadow
    abstract void setShoulderEntityLeft(NbtCompound entityNbt);

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        PlayerEntity entity = ((PlayerEntity) (Object) this);

        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries)) {
            switch (topLevelNbt) {
                case "DataVersion" ->
                        nbt.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
                case "Inventory" -> {
                    if (this.inventory instanceof IFilteredNbtList mixin)
                        nbt.put("Inventory", mixin.writeNbtFiltered(new NbtList(), path.toString(), registries));
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
                    if (this.enderChestInventory instanceof IFilteredNbtList mixin)
                        nbt.put("EnderItems", mixin.writeNbtFiltered(new NbtList(), path.toString(), registries));
                    else
                        nbt.put("EnderItems", this.enderChestInventory.toNbtList(registries));
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
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        PlayerEntity entity = ((PlayerEntity) (Object) this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {

            switch (topLevelNbt) {
                case "Inventory" -> {
                    NbtList nbtList = nbt.getList("Inventory", 10);
                    this.inventory.readNbt(nbtList);
                }
                case "SelectedItemSlot" -> this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
                case "SleepTimer" -> this.sleepTimer = nbt.getShort("SleepTimer");
                case "XpP" -> entity.experienceProgress = nbt.getFloat("XpP");
                case "XpLevel" -> entity.experienceLevel = nbt.getInt("XpLevel");
                case "XpTotal" -> entity.totalExperience = nbt.getInt("XpTotal");
                case "XpSeed" -> {
                    this.enchantingTableSeed = nbt.getInt("XpSeed");
                    if (this.enchantingTableSeed == 0) {
                        this.enchantingTableSeed = this.random.nextInt();
                    }
                }
                case "Score" -> entity.setScore(nbt.getInt("Score"));
                case "foodLevel", "foodTickTimer", "foodSaturationLevel", "foodExhaustionLevel" ->
                        this.hungerManager.readNbt(nbt);
                case "abilities" -> this.abilities.readNbt(nbt);
                case "EnderItems" -> {
                    if (nbt.contains("EnderItems", 9))
                        this.enderChestInventory.readNbtList(nbt.getList("EnderItems", 10), registries);
                }
                case "ShoulderEntityLeft" -> {
                    if (nbt.contains("ShoulderEntityLeft", 10))
                        this.setShoulderEntityLeft(nbt.getCompound("ShoulderEntityLeft"));
                }
                case "ShoulderEntityRight" -> {
                    if (nbt.contains("ShoulderEntityRight", 10))
                        this.setShoulderEntityRight(nbt.getCompound("ShoulderEntityRight"));
                }
                case "LastDeathLocation" -> {
                    if (nbt.contains("LastDeathLocation", 10)) {
                        DataResult<GlobalPos> var3 = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("LastDeathLocation"));
                        Logger var10002 = Radon.LOGGER;
                        Objects.requireNonNull(var10002);
                        entity.setLastDeathPos(var3.resultOrPartial(var10002::error));
                    }
                }
                default -> {
                    return false;
                }
            }


        }
        return true;
    }
}
