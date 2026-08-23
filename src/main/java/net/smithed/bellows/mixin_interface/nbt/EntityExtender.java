package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface EntityExtender {

    /**
     * Bypass of Entity::saveWithoutId, only saves nbt at specified path instead of all nbt.
     * @param output - (from vanilla)
     * @param path - path to nbt data
     * @return boolean - true if nbt path matched a valid tag, otherwise false
     */
    boolean bellows_saveWithoutIdFiltered(ValueOutput output, String path);

    /**
     * Bypass of Entity::load, only saves nbt at specified path instead of all nbt.
     * @param input - (from vanilla)
     * @param path - path to nbt data
     * @return boolean - true if nbt path matched a valid tag, otherwise false
     */
    boolean bellows_loadFiltered(ValueInput input, String path);
}
