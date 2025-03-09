package dev.smithed.radon.mixin.entity;

import dev.smithed.radon.Radon;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.world.World;
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
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        if (!super.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt)) {
            switch (topLevelNbt) {
                case "text" -> Text.Serialization.toJsonString(this.getText(), this.getRegistryManager());
                case "line_width" -> nbt.putInt("line_width", this.getLineWidth());
                case "background" -> nbt.putInt("background", this.getBackground());
                case "text_opacity" -> nbt.putByte("text_opacity", this.getTextOpacity());
                case "shadow", "see_through", "default_background", "alignment" -> {
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
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        DisplayEntity.TextDisplayEntity entity = ((DisplayEntity.TextDisplayEntity) (Object) this);

        if (!super.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt)) {
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
                        this.setBackground(nbt.getInt("background"));
                    }
                }
                case "text" -> {
                    if (nbt.contains("text", 8)) {
                        String string = nbt.getString("text");

                        try {
                            Text text = Text.Serialization.fromJson(string, this.getRegistryManager());
                            if (text != null) {
                                World var7 = entity.getWorld();
                                if (var7 instanceof ServerWorld serverWorld) {
                                    ServerCommandSource serverCommandSource = entity.getCommandSource(serverWorld).withLevel(2);
                                    Text text2 = Texts.parse(serverCommandSource, text, entity, 0);
                                    entity.setText(text2);
                                    break;
                                }
                                entity.setText(Text.empty());
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
        }
        return true;



    }
}
