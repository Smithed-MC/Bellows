package net.smithed.bellows.mixin_interface;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ICustomNBTMixin {

    boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt);
    boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt);
}
