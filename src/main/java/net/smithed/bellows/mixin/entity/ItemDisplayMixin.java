package net.smithed.bellows.mixin.entity;

import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Display.ItemDisplay.class)
public abstract class ItemDisplayMixin extends DisplayMixin {

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Display.ItemDisplay entity = ((Display.ItemDisplay) (Object) this);

        switch (topLevelNbt) {
            case "item" -> {
                ItemStack itemStack = entity.getItemStack();
                if (!itemStack.isEmpty()) {
                    output.store("item", ItemStack.CODEC, itemStack);
                }
            }
            case "item_display" -> output.store("item_display", ItemDisplayContext.CODEC, entity.getItemTransform());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Display.ItemDisplay entity = ((Display.ItemDisplay) (Object) this);

        switch (topLevelNbt) {
            case "item" -> entity.setItemStack(input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
            case "item_display" -> entity.setItemTransform(input.read("item_display", ItemDisplayContext.CODEC).orElse(ItemDisplayContext.NONE));
            default -> {
                return false;
            }
        }
        return true;
    }
}
