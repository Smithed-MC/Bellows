package net.smithed.bellows.mixin_interface;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface EntityExtender {

    boolean bellows_saveWithoutIdFiltered(ValueOutput output, String path);
    boolean bellows_loadFiltered(ValueInput output, String path);

}
