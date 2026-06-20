package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.Radon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayMixin extends DisplayEntityMixin {

    @Shadow abstract int getBackgroundColor();
    @Shadow abstract byte getTextOpacity();
    @Shadow abstract Component getText();
    @Shadow abstract int getLineWidth();
    @Shadow abstract  byte getFlags();
    @Shadow abstract void setLineWidth(int lineWidth);
    @Shadow abstract void setTextOpacity(byte textOpacity);
    @Shadow abstract void setBackgroundColor(int background);
    @Shadow static byte loadFlag(byte flags, CompoundTag nbt, String nbtKey, byte flag) {return 0;}

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        if (super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "text" -> Component.Serializer.toJson(this.getText(), this.registryAccess());
            case "line_width" -> nbt.putInt("line_width", this.getLineWidth());
            case "background" -> nbt.putInt("background", this.getBackgroundColor());
            case "text_opacity" -> nbt.putByte("text_opacity", this.getTextOpacity());
            case "shadow", "see_through", "default_background", "alignment" -> {
                byte b = this.getFlags();
                loadFlag(b, nbt, "shadow", (byte) 1);
                loadFlag(b, nbt, "see_through", (byte) 2);
                loadFlag(b, nbt, "default_background", (byte) 4);
                Display.TextDisplay.Align.CODEC.encodeStart(NbtOps.INSTANCE, Display.TextDisplay.getAlign(b)).result().ifPresent((nbtElement) -> {
                    nbt.put("alignment", nbtElement);
                });
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        Display.TextDisplay entity = ((Display.TextDisplay) (Object) this);
        if (super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
            return true;
        }
        switch (topLevelNbt) {
            case "line_width" -> {
                if (nbt.contains("line_width", 99)) {
                    this.setLineWidth(nbt.getInt("line_width"));
                }
            }
            case "text_opacity" -> {
                if (nbt.contains("text_opacity", 99)) {
                    this.setTextOpacity(nbt.getByte("text_opacity"));
                }
            }
            case "background" -> {
                if (nbt.contains("background", 99)) {
                    this.setBackgroundColor(nbt.getInt("background"));
                }
            }
            case "text" -> {
                if (nbt.contains("text", 8)) {
                    String string = nbt.getString("text");

                    try {
                        Component text = Component.Serializer.fromJson(string, this.registryAccess());
                        if (text != null) {
                            Level var7 = entity.level();
                            if (var7 instanceof ServerLevel serverWorld) {
                                CommandSourceStack serverCommandSource = entity.createCommandSourceStackForNameResolution(serverWorld).withPermission(2);
                                Component text2 = ComponentUtils.updateForEntity(serverCommandSource, text, entity, 0);
                                entity.setText(text2);
                                break;
                            }
                            entity.setText(Component.empty());
                        }
                    } catch (Exception var9) {
                        Radon.LOGGER.warn("Failed to parse display entity text {}", string, var9);
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
