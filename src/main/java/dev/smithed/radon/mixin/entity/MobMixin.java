package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntityMixin implements ICustomNBTMixin {

    @Shadow boolean persistenceRequired;
    @Shadow NonNullList<ItemStack> armorItems;
    @Shadow float[] handDropChances;
    @Shadow float[] armorDropChances;
    @Shadow long lootTableSeed;
    @Shadow @Final NonNullList<ItemStack> handItems;
    @Shadow ItemStack bodyArmorItem;
    @Shadow float bodyArmorDropChance;
    @Shadow Optional<ResourceKey<LootTable>> lootTable;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Mob entity = ((Mob) (Object) this);
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "CanPickUpLoot" -> nbt.putBoolean("CanPickUpLoot", entity.canPickUpLoot());
            case "PersistenceRequired" -> nbt.putBoolean("PersistenceRequired", this.persistenceRequired);
            case "ArmorItems" -> {
                ListTag nbtList = new ListTag();
                for (ItemStack itemStack : this.armorItems) {
                    if (!itemStack.isEmpty()) {
                        nbtList.add(itemStack.save(this.registryAccess()));
                    } else {
                        nbtList.add(new CompoundTag());
                    }
                }
                nbt.put("ArmorItems", nbtList);
            }
            case "ArmorDropChances" -> {
                ListTag nbtList2 = new ListTag();
                float[] var11 = this.armorDropChances;
                for (float f : var11) {
                    nbtList2.add(FloatTag.valueOf(f));
                }
                nbt.put("ArmorDropChances", nbtList2);
            }
            case "HandItems" -> {
                ListTag nbtList3 = new ListTag();
                for (ItemStack itemStack3 : this.handItems) {
                    if (!itemStack3.isEmpty()) {
                        nbtList3.add(itemStack3.save(this.registryAccess()));
                    } else {
                        nbtList3.add(new CompoundTag());
                    }
                }
                nbt.put("HandItems", nbtList3);
            }
            case "HandDropChances" -> {
                ListTag nbtList4 = new ListTag();
                float[] var16 = this.handDropChances;
                for (float g : var16) {
                    nbtList4.add(FloatTag.valueOf(g));
                }
                nbt.put("HandDropChances", nbtList4);
            }
            case "body_armor_item" -> {
                if (!this.bodyArmorItem.isEmpty()) {
                    nbt.put("body_armor_item", this.bodyArmorItem.save(this.registryAccess()));
                }
            }
            case "body_armor_drop_chance" -> {
                if (!this.bodyArmorItem.isEmpty()) {
                    nbt.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
                }
            }
            case "LeftHanded" -> nbt.putBoolean("LeftHanded", entity.isLeftHanded());
            case "DeathLootTable" -> {
                if (this.lootTable.isPresent()) {
                    nbt.putString("DeathLootTable", this.lootTable.get().location().toString());
                }
            }
            case "DeathLootTableSeed" -> {
                if (this.lootTableSeed != 0L) {
                    nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
                }
            }
            case "NoAI" -> {
                if (entity.isNoAi()) {
                    nbt.putBoolean("NoAI", entity.isNoAi());
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Mob entity = ((Mob) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        ListTag nbtList;
        switch (topLevelNbt) {
            case "CanPickUpLoot" -> entity.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
            case "PersistenceRequired" -> this.persistenceRequired = nbt.getBoolean("PersistenceRequired");
            case "ArmorItems" -> {
                CompoundTag nbtCompound;
                if (nbt.contains("ArmorItems", 9)) {
                    nbtList = nbt.getList("ArmorItems", 10);

                    for(int i = 0; i < this.armorItems.size(); ++i) {
                        nbtCompound = nbtList.getCompound(i);
                        this.armorItems.set(i, ItemStack.parseOptional(this.registryAccess(), nbtCompound));
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
                        CompoundTag nbtCompound = nbtList.getCompound(i);
                        this.handItems.set(i, ItemStack.parseOptional(this.registryAccess(), nbtCompound));
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
                    this.lootTable = Optional.of(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(nbt.getString("DeathLootTable"))));
                } else {
                    this.lootTable = Optional.empty();
                }
            }
            case "DeathLootTableSeed" -> this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
            case "NoAI" -> entity.setNoAi(nbt.getBoolean("NoAI"));
            default -> {
                return false;
            }
        }
        return true;
    }
}
