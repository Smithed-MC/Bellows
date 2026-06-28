package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IEntitySelectorReaderExtender;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.commands.arguments.selector.options.InvertableSetOptionState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Predicate<EntitySelectorParser> ALWAYS_AVAILABLE;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID;
    @Shadow private static void register(String name, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> predicate, Component description) {}
    @Shadow private static CommandSyntaxException rollbackAndThrow(final EntitySelectorParser parser, final int start, final DynamicCommandExceptionType type, final String argument) {return null;}

    /**
     * @author ImCoolYeah105, dragoncommands
     * This inject overwrites statically registered selector options to wrap extra data.
     * It may be better to inject data directly, but lambda support is suspect.
     */
    @Inject(method = "bootStrap()V", at = @At("TAIL"))
    private static void radon_bootStrap(CallbackInfo ci) {
        register("type", (parser) -> {
            InvertableSetOptionState state = parser.typeOption();
            parser.setSuggestions((b, m) -> {
                if (state.canParseNegativeElement()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b, String.valueOf('!'));
                }

                if (state.canParsePositiveElement()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b);
                }

                if (state.canParseAnyTag()) {
                    Stream<Identifier> var10000 = BuiltInRegistries.ENTITY_TYPE.getTags().map((tag) -> tag.key().location());
                    Objects.requireNonNull(state);
                    List<Identifier> allowedTags = var10000.filter(state::canParseTag).toList();
                    if (!allowedTags.isEmpty()) {
                        SharedSuggestionProvider.suggestResource(allowedTags, b, String.valueOf('#'));
                        SharedSuggestionProvider.suggestResource(allowedTags, b, "!#");
                    }
                }

                return b.buildFuture();
            });
            int start = parser.getReader().getCursor();
            boolean inverted = parser.shouldInvertValue();
            if (parser.isTag()) {
                if (!state.canParseAnyTag()) {
                    throw rollbackAndThrow(parser, start, ERROR_INAPPLICABLE_OPTION, "type");
                }

                Identifier id = Identifier.read(parser.getReader());
                if (!state.canParseTag(id)) {
                    throw rollbackAndThrow(parser, start, ERROR_INAPPLICABLE_OPTION, "type");
                }

                TagKey<@NotNull EntityType<?>> key = TagKey.create(Registries.ENTITY_TYPE, id);
                parser.addPredicate((e) -> e.is(key) != inverted);
                state.markParsedTag(id);

                // BEGIN INJECT
                if(Radon.CONFIG.entitySelectorOptimizations && parser instanceof IEntitySelectorReaderExtender entityExtender) {
                    entityExtender.radon_getSelectorContainer().type = key.location().toString();
                    entityExtender.radon_getSelectorContainer().isTypeTag = true;
                    entityExtender.radon_getSelectorContainer().isNotType = inverted;
                }
                // END INJECT
            } else {
                if (!state.canParseElement(inverted)) {
                    throw rollbackAndThrow(parser, start, ERROR_INAPPLICABLE_OPTION, "type");
                }

                Identifier id = Identifier.read(parser.getReader());
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow(() -> rollbackAndThrow(parser, start, ERROR_ENTITY_TYPE_INVALID, id.toString()));
                if (Objects.equals(EntityTypes.PLAYER, type) && !inverted) {
                    parser.setIncludesEntities(false);
                }

                parser.addPredicate((e) -> Objects.equals(type, e.getType()) != inverted);
                if (!inverted) {
                    parser.limitToType(type);
                }

                state.markParsedElement(inverted);

                // BEGIN INJECT
                if(Radon.CONFIG.entitySelectorOptimizations && parser instanceof IEntitySelectorReaderExtender entityExtender) {
                    entityExtender.radon_getSelectorContainer().type = id.getPath();
                    entityExtender.radon_getSelectorContainer().isTypeTag = false;
                    entityExtender.radon_getSelectorContainer().isNotType = inverted;
                }
                // END INJECT
            }

        }, (s) -> s.typeOption().canParseAny(), Component.translatable("argument.entity.options.type.description"));



        register("tag", (parser) -> {
            boolean inverted = parser.shouldInvertValue();
            String tag = parser.getReader().readUnquotedString();
            // BEGIN INJECT
            if(Radon.CONFIG.entitySelectorOptimizations && parser instanceof IEntitySelectorReaderExtender entityExtender) {
                if (inverted) {
                    entityExtender.radon_getSelectorContainer().notSelectorTags.add(tag);
                } else {
                    entityExtender.radon_getSelectorContainer().selectorTags.add(tag);
                }
            }
            // END INJECT
            parser.addPredicate((e) -> {
                if ("".equals(tag)) {
                    return e.entityTags().isEmpty() != inverted;
                } else {
                    return e.entityTags().contains(tag) != inverted;
                }
            });
        }, ALWAYS_AVAILABLE, Component.translatable("argument.entity.options.tag.description"));

        register("nbt", (parser) -> {
            boolean inverted = parser.shouldInvertValue();
            CompoundTag tag = TagParser.parseCompoundAsArgument(parser.getReader());
            parser.addPredicate((entity) -> {
                CompoundTag nbtCompound = null;
                if (Radon.CONFIG.nbtOptimizations && entity instanceof IEntityMixin mixin) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                        TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                        for (String str : NBTUtils.getTopLevelPaths(tag)) {
                            if (entity instanceof ServerPlayer player && str.startsWith("SelectedItem")) {
                                ItemStack selected = player.getInventory().getSelectedItem();
                                if (!selected.isEmpty()) {
                                    output.store("SelectedItem", ItemStack.CODEC, selected);
                                }
                            } else if(!mixin.radon_saveWithoutIdFiltered(output, str)) {
                                output = null;
                                break;
                            }
                        }
                        if(output != null) {
                            nbtCompound = output.buildResult();
                        }
                    }
                }

                // if getting the filtered data failed, try using the normal method
                if (nbtCompound == null) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                        TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                        entity.saveWithoutId(output);
                        if (entity instanceof ServerPlayer player) {
                            ItemStack selected = player.getInventory().getSelectedItem();
                            if (!selected.isEmpty()) {
                                output.store("SelectedItem", ItemStack.CODEC, selected);
                            }
                        }
                        Radon.logDebugFormat("nbt = %s", nbtCompound);
                        nbtCompound = output.buildResult();
                    }
                }

                Radon.logDebugFormat("nbt = %s", nbtCompound);
                return NbtUtils.compareNbt(tag, nbtCompound, true) != inverted;
            });
        }, ALWAYS_AVAILABLE, Component.translatable("argument.entity.options.nbt.description"));
    }

}
