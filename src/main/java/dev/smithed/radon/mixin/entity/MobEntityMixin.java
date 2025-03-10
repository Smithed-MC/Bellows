package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow boolean persistent;
    @Shadow DefaultedList<ItemStack> armorItems;
    @Shadow float[] handDropChances;
    @Shadow float[] armorDropChances;
    @Shadow long lootTableSeed;
    @Shadow @Final DefaultedList<ItemStack> handItems;
    @Shadow ItemStack bodyArmor;
    @Shadow float bodyArmorDropChance;
    @Shadow Optional<RegistryKey<LootTable>> lootTable;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        MobEntity entity = ((MobEntity) (Object) this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "CanPickUpLoot" -> nbt.putBoolean("CanPickUpLoot", entity.canPickUpLoot());
            case "PersistenceRequired" -> nbt.putBoolean("PersistenceRequired", this.persistent);
            case "ArmorItems" -> {
                NbtList nbtList = new NbtList();
                for (ItemStack itemStack : this.armorItems) {
                    if (!itemStack.isEmpty()) {
                        nbtList.add(itemStack.toNbt(this.getRegistryManager()));
                    } else {
                        nbtList.add(new NbtCompound());
                    }
                }
                nbt.put("ArmorItems", nbtList);
            }
            case "ArmorDropChances" -> {
                NbtList nbtList2 = new NbtList();
                float[] var11 = this.armorDropChances;
                for (float f : var11) {
                    nbtList2.add(NbtFloat.of(f));
                }
                nbt.put("ArmorDropChances", nbtList2);
            }
            case "HandItems" -> {
                NbtList nbtList3 = new NbtList();
                for (ItemStack itemStack3 : this.handItems) {
                    if (!itemStack3.isEmpty()) {
                        nbtList3.add(itemStack3.toNbt(this.getRegistryManager()));
                    } else {
                        nbtList3.add(new NbtCompound());
                    }
                }
                nbt.put("HandItems", nbtList3);
            }
            case "HandDropChances" -> {
                NbtList nbtList4 = new NbtList();
                float[] var16 = this.handDropChances;
                for (float g : var16) {
                    nbtList4.add(NbtFloat.of(g));
                }
                nbt.put("HandDropChances", nbtList4);
            }
            case "body_armor_item" -> {
                if (!this.bodyArmor.isEmpty()) {
                    nbt.put("body_armor_item", this.bodyArmor.toNbt(this.getRegistryManager()));
                }
            }
            case "body_armor_drop_chance" -> {
                if (!this.bodyArmor.isEmpty()) {
                    nbt.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
                }
            }
            case "LeftHanded" -> nbt.putBoolean("LeftHanded", entity.isLeftHanded());
            case "DeathLootTable" -> {
                if (this.lootTable.isPresent()) {
                    nbt.putString("DeathLootTable", this.lootTable.get().getValue().toString());
                }
            }
            case "DeathLootTableSeed" -> {
                if (this.lootTableSeed != 0L) {
                    nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
                }
            }
            case "NoAI" -> {
                if (entity.isAiDisabled()) {
                    nbt.putBoolean("NoAI", entity.isAiDisabled());
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
        MobEntity entity = ((MobEntity) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        NbtList nbtList;
        switch (topLevelNbt) {
            case "CanPickUpLoot" -> entity.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
            case "PersistenceRequired" -> this.persistent = nbt.getBoolean("PersistenceRequired");
            case "ArmorItems" -> {
                NbtCompound nbtCompound;
                if (nbt.contains("ArmorItems", 9)) {
                    nbtList = nbt.getList("ArmorItems", 10);

                    for(int i = 0; i < this.armorItems.size(); ++i) {
                        nbtCompound = nbtList.getCompound(i);
                        this.armorItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), nbtCompound));
                    }
                } else {
                    Collections.fill(this.armorItems, ItemStack.EMPTY);
                }
            }
            case "ArmorDropChances" -> {
                if (nbt.contains("ArmorDropChances", 9)) {
                    nbtList = nbt.getList("ArmorDropChances", 5);

                    for(int i = 0; i < nbtList.size(); ++i) {
                        this.armorDropChances[i] = nbtList.getFloat(i);
                    }
                } else {
                    Arrays.fill(this.armorDropChances, 0.0F);
                }
            }
            case "HandItems" -> {
                if (nbt.contains("HandItems", 9)) {
                    nbtList = nbt.getList("HandItems", 10);

                    for(int i = 0; i < this.handItems.size(); ++i) {
                        NbtCompound nbtCompound = nbtList.getCompound(i);
                        this.handItems.set(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), nbtCompound));
                    }
                } else {
                    Collections.fill(this.handItems, ItemStack.EMPTY);
                }
            }
            case "HandDropChances" -> {
                if (nbt.contains("HandDropChances", 9)) {
                    nbtList = nbt.getList("HandDropChances", 5);

                    for(int i = 0; i < nbtList.size(); ++i) {
                        this.handDropChances[i] = nbtList.getFloat(i);
                    }
                } else {
                    Arrays.fill(this.handDropChances, 0.0F);
                }
            }
            case "LeftHanded" -> entity.setLeftHanded(nbt.getBoolean("LeftHanded"));
            case "DeathLootTable" -> {
                if (nbt.contains("DeathLootTable", 8)) {
                    this.lootTable = Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(nbt.getString("DeathLootTable"))));
                } else {
                    this.lootTable = Optional.empty();
                }
            }
            case "DeathLootTableSeed" -> this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
            case "NoAI" -> entity.setAiDisabled(nbt.getBoolean("NoAI"));
            default -> {
                return false;
            }
        }
        return true;
    }
}
