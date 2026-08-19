package net.smithed.bellows.mixin.entity;

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
    @Shadow private static byte loadFlag(final byte flags, final ValueInput input, final String id, final byte mask) { return 0; }

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
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
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }
        Display.TextDisplay entity = ((Display.TextDisplay) (Object) this);

        switch (topLevelNbt) {
            case "line_width" -> entity.setLineWidth(input.getIntOr("line_width", 200));
            case "text_opacity" -> entity.setTextOpacity(input.getByteOr("text_opacity", (byte)-1));
            case "background" -> entity.setBackgroundColor(input.getIntOr("background", 1073741824));
            case "shadow" -> {
                byte flags = loadFlag((byte)0, input, "shadow", (byte)1);
                flags = loadFlag(flags, input, "see_through", (byte)2);
                flags = loadFlag(flags, input, "default_background", (byte)4);
                Optional<Display.TextDisplay.Align> alignment = input.read("alignment", Display.TextDisplay.Align.CODEC);
                if (alignment.isPresent()) {
                    byte var10000;
                    switch (alignment.get().ordinal()) {
                        case 0 -> var10000 = flags;
                        case 1 -> var10000 = (byte)(flags | 8);
                        case 2 -> var10000 = (byte)(flags | 16);
                        default -> throw new MatchException((String)null, (Throwable)null);
                    }

                    flags = var10000;
                }
                entity.setFlags(flags);
            }
            case "see_through" -> {} // these cases handled by 'shadow'
            case "default_background" -> {}
            case "alignment" -> {}
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
