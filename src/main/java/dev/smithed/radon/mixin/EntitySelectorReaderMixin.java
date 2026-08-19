package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IEntitySelectorExtender;
import dev.smithed.radon.mixin_interface.IEntitySelectorReaderExtender;
import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorReaderMixin implements IEntitySelectorReaderExtender {

    @Unique
    private final SelectorContainer container = new SelectorContainer();

    /**
     * @author ImCoolYeah105
     * get the constructed return value and inject additional data
     */
    @Inject(method = "parse", at = @At("RETURN"), cancellable = true)
    private void bellows_parse(CallbackInfoReturnable<EntitySelector> cir) {
        if(cir.getReturnValue() instanceof IEntitySelectorExtender extender) {
            extender.bellows_setContainer(this.container);
            cir.setReturnValue((EntitySelector) extender);
        }
    }

    @Override
    public SelectorContainer bellows_getSelectorContainer() {
        return this.container;
    }
}
