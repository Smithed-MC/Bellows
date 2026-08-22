package net.smithed.bellows.mixin_interface.nbt;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface FilteredNbtAccessExtender {

    /**
     * Bypass of Entity::addAdditionalSaveData, only stores nbt from specified path instead of all nbt.
     * @param output - (from vanilla) output to store data on
     * @param path - path to retrieve data from
     * @param topLevelNbt - preprocessed first nbt path level
     * @return boolean - true if nbt path matched a valid tag, otherwise false
     */
    boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt);

    /**
     * Bypass of Entity::addAdditionalSaveData, only stores nbt from specified path instead of all nbt.
     * @param input - (from vanilla) input to load data on
     * @param path - path to retrieve data from
     * @param topLevelNbt - preprocessed first nbt path level
     * @return boolean - true if nbt path matched a valid tag, otherwise false
     */
    boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt);
}
