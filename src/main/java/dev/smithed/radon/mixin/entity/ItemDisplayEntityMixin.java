package dev.smithed.radon.mixin.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;


@Mixin(DisplayEntity.ItemDisplayEntity.class)
public abstract class ItemDisplayEntityMixin extends DisplayEntityMixin {

    @Shadow abstract ItemStack getItemStack();
    @Shadow abstract ModelTransformationMode getTransformationMode();
    @Shadow abstract void setItemStack(ItemStack stack);
    @Shadow abstract void setTransformationMode(ModelTransformationMode transformationMode);

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "item" -> {
                if (!this.getItemStack().isEmpty()) {
                    nbt.put("item", this.getItemStack().toNbt(this.getRegistryManager()));
                }
            }
            case "item_display" ->
                ModelTransformationMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getTransformationMode()).ifSuccess((nbtx) -> nbt.put("item_display", nbtx));
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "item" -> {
                if (nbt.contains("item")) {
                    this.setItemStack(ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("item")).orElse(ItemStack.EMPTY));
                } else {
                    this.setItemStack(ItemStack.EMPTY);
                }
            }
            case "item_display" -> {
                if (nbt.contains("item_display", 8)) {
                    DataResult<Pair<ModelTransformationMode, NbtElement>> var10000 = ModelTransformationMode.CODEC.decode(NbtOps.INSTANCE, nbt.get("item_display"));
                    Logger var10002 = LOGGER;
                    Objects.requireNonNull(var10002);
                    var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((mode) -> {
                        this.setTransformationMode(mode.getFirst());
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
