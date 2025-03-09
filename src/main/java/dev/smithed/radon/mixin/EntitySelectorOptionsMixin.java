package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IEntitySelectorReaderExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {
    @Shadow
    private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {}

    /**
     * @author ImCoolYeah105, dragoncommands
     * This inject overwrites statically registered selector options to wrap extra data.
     * It may be better to inject data directly, but lambda support is suspect.
     */
    @Inject(method = "register()V", at = @At("TAIL"))
    private static void radon_register(CallbackInfo ci) {
        putOption("type", (reader) -> {
            reader.setSuggestionProvider((builder, consumer) -> {
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder, String.valueOf('!'));
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(t -> t.getTag().id()), builder, "!#");
                if (!reader.excludesEntityType()) {
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder);
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(t -> t.getTag().id()), builder, String.valueOf('#'));
                }

                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesEntityType() && !bl) {
                reader.getReader().setCursor(i);
                throw EntitySelectorOptions.INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "type");
            } else {
                if (bl) {
                    reader.setExcludesEntityType();
                }

                if (reader.readTagCharacter()) {
                    TagKey<EntityType<?>> tagKey = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.fromCommandInput(reader.getReader()));
                    if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext) {
                        entityext.getSelectorContainer().type = tagKey.id().toString();
                        entityext.getSelectorContainer().isTypeTag = true;
                        entityext.getSelectorContainer().isNotType = bl;
                    }
                    reader.addPredicate((entity) -> entity.getType().isIn(tagKey) != bl);
                } else {
                    Identifier identifier = Identifier.fromCommandInput(reader.getReader());

                    EntityType<?> entityType = Registries.ENTITY_TYPE.getOptionalValue(identifier).orElseThrow(() -> {
                        reader.getReader().setCursor(i);
                        return EntitySelectorOptions.INVALID_TYPE_EXCEPTION.createWithContext(reader.getReader(), identifier.toString());
                    });

                    if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                        reader.setIncludesNonPlayers(false);
                    }

                    if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext) {
                        entityext.getSelectorContainer().type = identifier.toString();
                        entityext.getSelectorContainer().isTypeTag = false;
                        entityext.getSelectorContainer().isNotType = bl;
                    }

                    reader.addPredicate((entity) -> Objects.equals(entityType, entity.getType()) != bl);
                    if (!bl) {
                        reader.setEntityType(entityType);
                    }
                }

            }
        }, (reader) -> !reader.selectsEntityType(), Text.translatable("argument.entity.options.type.description"));

        putOption("tag", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext)
                if(bl)
                    entityext.getSelectorContainer().notSelectorTags.add(string);
                else
                    entityext.getSelectorContainer().selectorTags.add(string);
            reader.addPredicate((entity) -> {
                if ("".equals(string)) {
                    return entity.getCommandTags().isEmpty() != bl;
                } else {
                    return entity.getCommandTags().contains(string) != bl;
                }
            });
        }, (reader) -> true, Text.translatable("argument.entity.options.tag.description"));

        putOption("nbt", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            NbtCompound nbtCompound = (new StringNbtReader(reader.getReader())).parseCompound();
            reader.addPredicate((entity) -> {
                NbtCompound nbtCompound2 = null;
                if(Radon.CONFIG.nbtOptimizations && entity instanceof IEntityMixin mixin) {
                    nbtCompound2 = new NbtCompound();
                    String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbtCompound);
                    for(String nbt: topLevelNbt) {
                        if (entity instanceof ServerPlayerEntity player && nbt.equals("SelectedItem")) {
                            ItemStack itemStack = player.getInventory().getMainHandStack();
                            if (!itemStack.isEmpty()) {
                                nbtCompound2.put("SelectedItem", itemStack.toNbt(entity.getRegistryManager(), new NbtCompound()));
                            }
                        } else {
                            nbtCompound2 = mixin.writeNbtFiltered(nbtCompound2, nbt);
                            if (nbtCompound2 == null)
                                break;
                        }
                    }
                }
                if(nbtCompound2 == null) {
                    nbtCompound2 = entity.writeNbt(new NbtCompound());
                    if (entity instanceof ServerPlayerEntity player) {
                        ItemStack itemStack = player.getInventory().getMainHandStack();
                        if (!itemStack.isEmpty()) {
                            nbtCompound2.put("SelectedItem", itemStack.toNbt(entity.getRegistryManager(), new NbtCompound()));
                        }
                    }
                }
                Radon.logDebugFormat("nbt = %s", nbtCompound);
                return NbtHelper.matches(nbtCompound, nbtCompound2, true) != bl;
            });
        }, (reader) -> true, Text.translatable("argument.entity.options.nbt.description"));
    }

}
