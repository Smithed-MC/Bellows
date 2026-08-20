package net.smithed.bellows.mixin.nbt;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.smithed.bellows.mixin_interface.CompoundTagExtender;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompoundTagArgument.class)
public class CompoundTagArgumentMixin {

    @ModifyReturnValue(method = "parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private CompoundTag bellows_parse(CompoundTag tag) {
        if(tag instanceof CompoundTagExtender extender) {
            extender.bellows_precompileQuickActions();
        }
        return tag;
    }
}
