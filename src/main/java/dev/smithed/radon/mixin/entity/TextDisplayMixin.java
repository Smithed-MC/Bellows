package dev.smithed.radon.mixin.entity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayMixin extends DisplayEntityMixin {

    @Shadow private static void storeFlag(final byte flags, final ValueOutput output, final String id, final byte mask) {}

    @Override
    public boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.radon_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }
        Display.TextDisplay entity = ((Display.TextDisplay) (Object) this);

        switch (topLevelNbt) {
            case "text" -> output.store("text", ComponentSerialization.CODEC, entity.getText());
            case "line_width" -> output.putInt("line_width", entity.getLineWidth());
            case "background" -> output.putInt("background", entity.getBackgroundColor());
            case "text_opacity" -> output.putByte("text_opacity", entity.getTextOpacity());
            case "shadow", "see_through", "default_background", "alignment" -> {
                byte flags = entity.getFlags();
                storeFlag(flags, output, "shadow", (byte)1);
                storeFlag(flags, output, "see_through", (byte)2);
                storeFlag(flags, output, "default_background", (byte)4);
                output.store("alignment", Display.TextDisplay.Align.CODEC, Display.TextDisplay.getAlign(flags));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean radon_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.radon_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Display.TextDisplay entity = ((Display.TextDisplay) (Object) this);

        switch (topLevelNbt) {
            case "line_width" -> entity.setLineWidth(input.getIntOr("line_width", 200));
            case "text_opacity" -> entity.setTextOpacity(input.getByteOr("text_opacity", (byte)-1));
            case "background" -> entity.setBackgroundColor(input.getIntOr("background", 1073741824));
            case "text" -> {
                Optional<Component> text = input.read("text", ComponentSerialization.CODEC);
                if (text.isPresent()) {
                    try {
                        Level var6 = entity.level();
                        if (var6 instanceof ServerLevel) {
                            ServerLevel serverLevel = (ServerLevel)var6;
                            CommandSourceStack context = entity.createCommandSourceStackForNameResolution(serverLevel).withPermission(LevelBasedPermissionSet.GAMEMASTER);
                            Component resolvedText = ComponentUtils.resolve(ResolutionContext.create(context), (Component)text.get());
                            entity.setText(resolvedText);
                        } else {
                            entity.setText(Component.empty());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to parse display entity text {}", text, e);
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
