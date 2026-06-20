package dev.smithed.radon.mixin.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;


@Mixin(Display.ItemDisplay.class)
public abstract class ItemDisplayMixin extends DisplayEntityMixin {

    @Shadow abstract ItemStack getItemStack();
    @Shadow abstract ItemDisplayContext getItemTransform();
    @Shadow abstract void setItemStack(ItemStack stack);
    @Shadow abstract void setItemTransform(ItemDisplayContext transformationMode);

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "item" -> {
                if (!this.getItemStack().isEmpty()) {
                    nbt.put("item", this.getItemStack().save(this.registryAccess()));
                }
            }
            case "item_display" ->
                ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).ifSuccess((nbtx) -> nbt.put("item_display", nbtx));
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "item" -> {
                if (nbt.contains("item")) {
                    this.setItemStack(ItemStack.parse(this.registryAccess(), nbt.getCompound("item")).orElse(ItemStack.EMPTY));
                } else {
                    this.setItemStack(ItemStack.EMPTY);
                }
            }
            case "item_display" -> {
                if (nbt.contains("item_display", 8)) {
                    DataResult<Pair<ItemDisplayContext, Tag>> var10000 = ItemDisplayContext.CODEC.decode(NbtOps.INSTANCE, nbt.get("item_display"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.prefix("Display entity", var10002::error)).ifPresent((mode) -> {
                        this.setItemTransform(mode.getFirst());
                    });
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
