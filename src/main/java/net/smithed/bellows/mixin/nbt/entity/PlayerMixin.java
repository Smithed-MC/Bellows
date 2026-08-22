package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.storage.ValueOutput;
import net.smithed.bellows.mixin_interface.nbt.EnderChestInventoryExtender;
import net.smithed.bellows.mixin_interface.nbt.FilteredNbtAccessExtender;
import net.smithed.bellows.mixin_interface.nbt.PlayerInventoryExtender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements FilteredNbtAccessExtender {

    @Shadow @Final
    private Abilities abilities;
    @Shadow @Final
    private Inventory inventory;
    @Shadow
    private int sleepCounter;
    @Shadow
    protected int enchantmentSeed;
    @Shadow
    protected FoodData foodData;
    @Shadow
    protected PlayerEnderChestContainer enderChestInventory;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Player entity = ((Player) (Object) this);

        switch (topLevelNbt) {
            case "DataVersion" -> NbtUtils.addCurrentDataVersion(output);
            case "Inventory" -> {
                if (this.inventory instanceof PlayerInventoryExtender mixin) {
                    mixin.bellows_saveFiltered(output.list("Inventory", ItemStackWithSlot.CODEC), path);
                } else {
                    this.inventory.save(output.list("Inventory", ItemStackWithSlot.CODEC));
                }
            }
            case "SelectedItemSlot" -> output.putInt("SelectedItemSlot", this.inventory.getSelectedSlot());
            case "SleepTimer" -> output.putShort("SleepTimer", (short)this.sleepCounter);
            case "XpP" -> output.putFloat("XpP", entity.experienceProgress);
            case "XpLevel" -> output.putInt("XpLevel", entity.experienceLevel);
            case "XpTotal" -> output.putInt("XpTotal", entity.totalExperience);
            case "XpSeed" -> output.putInt("XpSeed", this.enchantmentSeed);
            case "Score" -> output.putInt("Score", entity.getScore());
            case "foodLevel", "foodTickTimer", "foodSaturationLevel", "foodExhaustionLevel" -> this.foodData.addAdditionalSaveData(output);
            case "abilities" -> output.store("abilities", Abilities.Packed.CODEC, this.abilities.pack());
            case "EnderItems" -> {
                if (this.enderChestInventory instanceof EnderChestInventoryExtender mixin)
                    mixin.bellows_storeAsSlotsFiltered(output.list("EnderItems", ItemStackWithSlot.CODEC), path);
                else
                    this.enderChestInventory.storeAsSlots(output.list("EnderItems", ItemStackWithSlot.CODEC));
            }
            case "LastDeathLocation" -> entity.getLastDeathLocation().ifPresent((pos) -> output.store("LastDeathLocation", GlobalPos.CODEC, pos));
            default -> {
                return false;
            }
        }
        return true;
    }
}
