package dev.smithed.radon.mixin_interface;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface EntityExtender {

    boolean radon_saveWithoutIdFiltered(ValueOutput output, String path);
    boolean radon_loadFiltered(ValueInput output, String path);

}
