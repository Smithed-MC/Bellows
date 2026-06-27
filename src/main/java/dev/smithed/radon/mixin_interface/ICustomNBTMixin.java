package dev.smithed.radon.mixin_interface;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ICustomNBTMixin {

    //write
    boolean radon_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt);
    //read
    boolean radon_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt);

}
