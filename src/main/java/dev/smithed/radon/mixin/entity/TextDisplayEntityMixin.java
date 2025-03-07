package dev.smithed.radon.mixin.entity;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayEntityMixin extends DisplayEntityMixin {

    @Shadow abstract int getBackground();
    @Shadow abstract byte getTextOpacity();
    @Shadow abstract Text getText();
    @Shadow abstract int getLineWidth();
    @Shadow abstract  byte getDisplayFlags();
    @Shadow abstract void setLineWidth(int lineWidth);
    @Shadow abstract void setTextOpacity(byte textOpacity);
    @Shadow abstract void setBackground(int background);
    @Shadow static void writeFlag(byte flags, NbtCompound nbt, String nbtKey, byte flag) {}

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt, registries)) {
            switch (topLevelNbt) {
                case "text" -> nbt.putString("text", Text.Serialization.toJsonString(this.getText(), registries));
                case "line_width" -> nbt.putInt("line_width", this.getLineWidth());
                case "background" -> nbt.putInt("background", this.getBackground());
                case "text_opacity" -> nbt.putByte("text_opacity", this.getTextOpacity());
                case "alignment" -> {
                    byte b = this.getDisplayFlags();
                    writeFlag(b, nbt, "shadow", (byte) 1);
                    writeFlag(b, nbt, "see_through", (byte) 2);
                    writeFlag(b, nbt, "default_background", (byte) 4);
                    DisplayEntity.TextDisplayEntity.TextAlignment.CODEC.encodeStart(NbtOps.INSTANCE, DisplayEntity.TextDisplayEntity.getAlignment(b)).result().ifPresent((nbtElement) -> {
                        nbt.put("alignment", nbtElement);
                    });
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt, RegistryWrapper.WrapperLookup registries) {
        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt, registries)) {

            switch (topLevelNbt) {
                case "line_width" -> {
                    if (nbt.contains("line_width", 99)) {
                        this.setLineWidth(nbt.getInt("line_width"));
                    }
                }
                case "text_opacity" -> {
                    if (nbt.contains("text_opacity", 99)) {
                        this.setTextOpacity(nbt.getByte("text_opacity"));
                    }}
                case "background" -> {
                    if (nbt.contains("background", 99)) {
                        this.setBackground(nbt.getInt("background"));
                    }}
                default -> {
                    return false;
                }
            }
        }
        return true;



    }
}
