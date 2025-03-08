package dev.smithed.radon.mixin;

import dev.smithed.radon.mixin_interface.IEntitySelectorExtender;
import dev.smithed.radon.mixin_interface.IEntitySelectorReaderExtender;
import dev.smithed.radon.utils.SelectorContainer;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements IEntitySelectorReaderExtender {

    /**
     * @author ImCoolYeah105
     * get the constructed return value and inject additional data
     */
    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void radon_build(CallbackInfoReturnable<EntitySelector> cir) {
        if(cir.getReturnValue() instanceof IEntitySelectorExtender extender) {
            extender.setContainer(this.container);
            cir.setReturnValue((EntitySelector) extender);
        }
    }

    private final SelectorContainer container = new SelectorContainer();

    @Override
    public SelectorContainer getSelectorContainer() {
        return this.container;
    }
}
