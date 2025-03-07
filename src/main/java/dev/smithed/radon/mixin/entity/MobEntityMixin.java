package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow
    boolean persistent;
    @Shadow
    DefaultedList<ItemStack> armorItems;
    @Shadow
    DefaultedList<ItemStack> handItems;
    @Shadow
    float[] handDropChances;
    @Shadow
    float[] armorDropChances;
    @Shadow
    private Optional<RegistryKey<LootTable>> lootTable;
    @Shadow
    long lootTableSeed;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MobEntity entity = ((MobEntity) (Object) this);
        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries)) {
            switch (topLevelNbt) {
                case "CanPickUpLoot":
                    nbt.putBoolean("CanPickUpLoot", entity.canPickUpLoot());
                    break;
                case "PersistenceRequired":
                    nbt.putBoolean("PersistenceRequired", this.persistent);
                    break;
                case "ArmorItems":
                    NbtList nbtList = new NbtList();
                    NbtCompound nbtCompound;
                    for (Iterator<ItemStack> var3 = this.armorItems.iterator(); var3.hasNext(); nbtList.add(nbtCompound)) {
                        ItemStack itemStack = (ItemStack) var3.next();
                        nbtCompound = new NbtCompound();
                        if (!itemStack.isEmpty()) {
                            itemStack.toNbt(registries, nbtCompound);
                        }
                    }
                    nbt.put("ArmorItems", nbtList);
                    break;
                case "HandItems":
                    NbtList nbtList2 = new NbtList();
                    NbtCompound nbtCompound2;
                    for (Iterator<ItemStack> var11 = this.handItems.iterator(); var11.hasNext(); nbtList2.add(nbtCompound2)) {
                        ItemStack itemStack2 = (ItemStack) var11.next();
                        nbtCompound2 = new NbtCompound();
                        if (!itemStack2.isEmpty()) {
                            itemStack2.toNbt(registries, nbtCompound2);
                        }
                    }
                    nbt.put("HandItems", nbtList2);
                    break;
                case "ArmorDropChances":
                    NbtList nbtList3 = new NbtList();
                    float[] var14 = this.armorDropChances;
                    int var16 = var14.length;
                    int var7;
                    for (var7 = 0; var7 < var16; ++var7) {
                        float f = var14[var7];
                        nbtList3.add(NbtFloat.of(f));
                    }
                    nbt.put("ArmorDropChances", nbtList3);
                    break;
                case "HandDropChances":
                    NbtList nbtList4 = new NbtList();
                    float[] var17 = this.handDropChances;
                    var7 = var17.length;

                    for (int var19 = 0; var19 < var7; ++var19) {
                        float g = var17[var19];
                        nbtList4.add(NbtFloat.of(g));
                    }

                    nbt.put("HandDropChances", nbtList4);
                    break;
                case "LeftHanded":
                    nbt.putBoolean("LeftHanded", entity.isLeftHanded());
                    break;
                case "DeathLootTable":
                    if (this.lootTable != null) {
                        nbt.putString("DeathLootTable", this.lootTable.toString());
                        if (this.lootTableSeed != 0L) {
                            nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
                        }
                    }
                    break;
                case "NoAI":
                    if (entity.isAiDisabled()) {
                        nbt.putBoolean("NoAI", entity.isAiDisabled());
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        MobEntity entity = ((MobEntity) (Object) this);
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {

            NbtList nbtList;
            switch (topLevelNbt) {
                case "CanPickUpLoot" -> {
                    if (nbt.contains("CanPickUpLoot", 1))
                        entity.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
                }
                case "PersistenceRequired" -> this.persistent = nbt.getBoolean("PersistenceRequired");
                case "ArmorItems" -> {
                    if (nbt.contains("ArmorItems", 9)) {
                        nbtList = nbt.getList("ArmorItems", 10);
                        for (int i = 0; i < this.armorItems.size(); ++i) {
                            int finalI = i;
                            ItemStack.fromNbt(registries, nbtList.getCompound(i)).ifPresent(item -> this.armorItems.set(finalI, item));
                        }
                    }
                }
                case "HandItems" -> {
                    if (nbt.contains("HandItems", 9)) {
                        nbtList = nbt.getList("HandItems", 10);
                        for (int i = 0; i < this.handItems.size(); ++i) {

                            int finalI = i;
                            ItemStack.fromNbt(registries, nbtList.getCompound(i)).ifPresent(item -> this.handItems.set(finalI, item));
                        }
                    }
                }
                case "ArmorDropChances" -> {
                    if (nbt.contains("ArmorDropChances", 9)) {
                        nbtList = nbt.getList("ArmorDropChances", 5);
                        for (int i = 0; i < nbtList.size(); ++i) {
                            this.armorDropChances[i] = nbtList.getFloat(i);
                        }
                    }
                }
                case "HandDropChances" -> {
                    if (nbt.contains("HandDropChances", 9)) {
                        nbtList = nbt.getList("HandDropChances", 5);
                        for (int i = 0; i < nbtList.size(); ++i) {
                            this.handDropChances[i] = nbtList.getFloat(i);
                        }
                    }
                }
                case "LeftHanded" -> entity.setLeftHanded(nbt.getBoolean("LeftHanded"));
                case "DeathLootTableSeed" -> this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
                case "NoAI" -> entity.setAiDisabled(nbt.getBoolean("NoAI"));
                default -> {
                    return false;
                }
            }
        }
        return true;
    }
}
