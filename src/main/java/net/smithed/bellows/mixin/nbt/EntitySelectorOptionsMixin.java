package net.smithed.bellows.mixin.nbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;
import net.smithed.bellows.utils.NBTUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    private static Predicate<EntitySelectorParser> ALWAYS_AVAILABLE;
    @Shadow @Final
    public static DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION;
    @Shadow @Final
    public static DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID;
    @Shadow
    private static void register(String name, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> predicate, Component description) {}
    @Shadow
    private static CommandSyntaxException rollbackAndThrow(final EntitySelectorParser parser, final int start, final DynamicCommandExceptionType type, final String argument) {return null;}

    /**
     * This inject overwrites statically registered selector options to wrap extra data.
     * It may be better to inject data directly, but lambda support is suspect.
     * @author ICY105
     */
    @Inject(method = "bootStrap()V", at = @At("TAIL"))
    private static void bellows_bootStrap(CallbackInfo ci) {
        register("nbt", (parser) -> {
            boolean inverted = parser.shouldInvertValue();
            CompoundTag tag = TagParser.parseCompoundAsArgument(parser.getReader());
            parser.addPredicate((entity) -> {
                CompoundTag nbtCompound = null;
                if (Bellows.CONFIG.nbtOptimizations && entity instanceof EntityExtender mixin) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                        TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                        for (String str: NBTUtils.getTopLevelPaths(tag)) {
                            if(output.buildResult().contains(str)) {
                                continue;
                            }
                            if (entity instanceof ServerPlayer player && str.startsWith("SelectedItem")) {
                                ItemStack selected = player.getInventory().getSelectedItem();
                                if (!selected.isEmpty()) {
                                    output.store("SelectedItem", ItemStack.CODEC, selected);
                                }
                            } else if(!mixin.bellows_saveWithoutIdFiltered(output, str)) {
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
                        Bellows.logDebugFormat("nbt = %s", nbtCompound);
                        nbtCompound = output.buildResult();
                    }
                }

                Bellows.logDebugFormat("nbt = %s", nbtCompound);
                return NbtUtils.compareNbt(tag, nbtCompound, true) != inverted;
            });
        }, ALWAYS_AVAILABLE, Component.translatable("argument.entity.options.nbt.description"));
    }
}
