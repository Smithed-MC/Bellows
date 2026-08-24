package net.smithed.bellows.mixin.selector;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.selector.EntitySelectorParserExtender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

    /**
     * Injects into "type" selector option to store entity type tags onto type/tag container.
     * @param parser - (from vanilla)
     * @param ci - callback info
     * @param inverted - is option inverted
     * @param key - type tag key
     */
    @Inject(method = "lambda$bootStrap$35", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/options/InvertableSetOptionState;markParsedTag(Lnet/minecraft/resources/Identifier;)V"))
    private static void bellows_bootstrap_typeTag(EntitySelectorParser parser, CallbackInfo ci, @Local(name = "inverted") boolean inverted, @Local(name = "key") TagKey<EntityType<?>> key) {
        if(Bellows.CONFIG.entitySelectorOptimizations && parser instanceof EntitySelectorParserExtender entityExtender) {
            entityExtender.bellows_getSelectorContainer().type = key.location().toString();
            entityExtender.bellows_getSelectorContainer().isTypeTag = true;
            entityExtender.bellows_getSelectorContainer().isNotType = inverted;
        }
    }

    /**
     * Injects into "type"" selector option to store entity type onto type/tag container.
     * @param parser - (from vanilla)
     * @param ci - callback info
     * @param inverted - is option inverted
     * @param id - identifier of tag
     */
    @Inject(method = "lambda$bootStrap$35", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/options/InvertableSetOptionState;markParsedElement(Z)V"))
    private static void bellows_bootstrap_type(EntitySelectorParser parser, CallbackInfo ci, @Local(name = "inverted") boolean inverted, @Local(name = "id") Identifier id) {
        if(Bellows.CONFIG.entitySelectorOptimizations && parser instanceof EntitySelectorParserExtender entityExtender) {
            entityExtender.bellows_getSelectorContainer().type = id.toString();
            entityExtender.bellows_getSelectorContainer().isTypeTag = false;
            entityExtender.bellows_getSelectorContainer().isNotType = inverted;
        }
    }

    /**
     * Injects into "tag" selector option to store tags onto type/tag container.
     * @param parser - (from vanilla)
     * @param ci - callback info
     * @param inverted - is option inverted
     * @param tag - tag to store
     */
    @Inject(method = "lambda$bootStrap$42", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;addPredicate(Ljava/util/function/Predicate;)V"))
    private static void bellows_bootstrap_tag(EntitySelectorParser parser, CallbackInfo ci, @Local(name = "inverted") boolean inverted, @Local(name = "tag") String tag) {
        if(Bellows.CONFIG.entitySelectorOptimizations && parser instanceof EntitySelectorParserExtender entityExtender) {
            if (inverted) {
                entityExtender.bellows_getSelectorContainer().notSelectorTags.add(tag);
            } else {
                entityExtender.bellows_getSelectorContainer().selectorTags.add(tag);
            }
        }
    }
}
