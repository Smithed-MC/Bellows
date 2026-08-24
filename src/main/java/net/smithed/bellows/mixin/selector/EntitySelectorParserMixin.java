package net.smithed.bellows.mixin.selector;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.smithed.bellows.mixin_interface.selector.EntitySelectorExtender;
import net.smithed.bellows.mixin_interface.selector.EntitySelectorParserExtender;
import net.smithed.bellows.utils.SelectorContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorParserMixin implements EntitySelectorParserExtender {

    @Unique
    private final SelectorContainer container = new SelectorContainer();

    /**
     * Gets the constructed value from parse and inserts the type/tag container.
     * @author ICY105
     * @param cir - callback info
     */
    @Inject(method = "parse()Lnet/minecraft/commands/arguments/selector/EntitySelector;", at = @At("RETURN"), cancellable = true)
    private void bellows_parse(CallbackInfoReturnable<EntitySelector> cir) {
        if(cir.getReturnValue() instanceof EntitySelectorExtender extender) {
            extender.bellows_setContainer(this.container);
            cir.setReturnValue((EntitySelector) extender);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectorContainer bellows_getSelectorContainer() {
        return this.container;
    }
}
