package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.mixin_interface.IEntitySelectorReaderExtender;
import dev.smithed.radon.utils.NBTUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {
    @Shadow
    private static void register(String id, EntitySelectorOptions.Modifier handler, Predicate<EntitySelectorParser> condition, Component description) {}

    /**
     * @author ImCoolYeah105, dragoncommands
     * This inject overwrites statically registered selector options to wrap extra data.
     * It may be better to inject data directly, but lambda support is suspect.
     */
    @Inject(method = "bootStrap()V", at = @At("TAIL"))
    private static void radon_bootStrap(CallbackInfo ci) {
        register("type", (reader) -> {
            reader.setSuggestions((builder, consumer) -> {
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), builder, String.valueOf('!'));
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(t -> t.key().location()), builder, "!#");
                if (!reader.isTypeLimitedInversely()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), builder);
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(t -> t.key().location()), builder, String.valueOf('#'));
                }

                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.shouldInvertValue();
            if (reader.isTypeLimitedInversely() && !bl) {
                reader.getReader().setCursor(i);
                throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.createWithContext(reader.getReader(), "type");
            } else {
                if (bl) {
                    reader.setTypeLimitedInversely();
                }

                if (reader.isTag()) {
                    TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(reader.getReader()));
                    if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext) {
                        entityext.getSelectorContainer().type = tagKey.location().toString();
                        entityext.getSelectorContainer().isTypeTag = true;
                        entityext.getSelectorContainer().isNotType = bl;
                    }
                    reader.addPredicate((entity) -> entity.getType().is(tagKey) != bl);
                } else {
                    ResourceLocation identifier = ResourceLocation.read(reader.getReader());

                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(identifier).orElseThrow(() -> {
                        reader.getReader().setCursor(i);
                        return EntitySelectorOptions.ERROR_ENTITY_TYPE_INVALID.createWithContext(reader.getReader(), identifier.toString());
                    });

                    if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                        reader.setIncludesEntities(false);
                    }

                    if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext) {
                        entityext.getSelectorContainer().type = identifier.toString();
                        entityext.getSelectorContainer().isTypeTag = false;
                        entityext.getSelectorContainer().isNotType = bl;
                    }

                    reader.addPredicate((entity) -> Objects.equals(entityType, entity.getType()) != bl);
                    if (!bl) {
                        reader.limitToType(entityType);
                    }
                }

            }
        }, (reader) -> !reader.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));

        register("tag", (reader) -> {
            boolean bl = reader.shouldInvertValue();
            String string = reader.getReader().readUnquotedString();
            if(Radon.CONFIG.entitySelectorOptimizations && reader instanceof IEntitySelectorReaderExtender entityext)
                if(bl)
                    entityext.getSelectorContainer().notSelectorTags.add(string);
                else
                    entityext.getSelectorContainer().selectorTags.add(string);
            reader.addPredicate((entity) -> {
                if ("".equals(string)) {
                    return entity.getTags().isEmpty() != bl;
                } else {
                    return entity.getTags().contains(string) != bl;
                }
            });
        }, (reader) -> true, Component.translatable("argument.entity.options.tag.description"));

        register("nbt", (reader) -> {
            boolean bl = reader.shouldInvertValue();
            CompoundTag nbtCompound = (new TagParser(reader.getReader())).readStruct();
            reader.addPredicate((entity) -> {
                CompoundTag nbtCompound2 = null;
                if(Radon.CONFIG.nbtOptimizations && entity instanceof IEntityMixin mixin) {
                    nbtCompound2 = new CompoundTag();
                    String[] topLevelNbt = NBTUtils.getTopLevelPaths(nbtCompound);
                    for(String nbt: topLevelNbt) {
                        if (entity instanceof ServerPlayer player && nbt.equals("SelectedItem")) {
                            ItemStack itemStack = player.getInventory().getSelected();
                            if (!itemStack.isEmpty()) {
                                nbtCompound2.put("SelectedItem", itemStack.save(entity.registryAccess(), new CompoundTag()));
                            }
                        } else {
                            nbtCompound2 = mixin.saveWithoutIdFiltered(nbtCompound2, nbt);
                            if (nbtCompound2 == null)
                                break;
                        }
                    }
                }
                if(nbtCompound2 == null) {
                    nbtCompound2 = entity.saveWithoutId(new CompoundTag());
                    if (entity instanceof ServerPlayer player) {
                        ItemStack itemStack = player.getInventory().getSelected();
                        if (!itemStack.isEmpty()) {
                            nbtCompound2.put("SelectedItem", itemStack.save(entity.registryAccess(), new CompoundTag()));
                        }
                    }
                }
                Radon.logDebugFormat("nbt = %s", nbtCompound);
                return NbtUtils.compareNbt(nbtCompound, nbtCompound2, true) != bl;
            });
        }, (reader) -> true, Component.translatable("argument.entity.options.nbt.description"));
    }

}
