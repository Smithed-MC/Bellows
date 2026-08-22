package net.smithed.bellows.mixin_interface.nbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityDataAccessorExtender {

    /**
     * Bypass of EntityDataAccessor::getData, retrieves specific nbt data from an entity instead of all nbt data.
     * @param path - path to requested data
     * @return CompoundTag - (from vanilla) requested nbt data
     * @throws CommandSyntaxException - (from vanilla) error occurs executing command
     */
    CompoundTag bellows_getDataFiltered(String path) throws CommandSyntaxException;

    /**
     * Bypass of EntityDataAccessor::setData, sets specific nbt data from an entity instead of all nbt data.
     * @param path - path to data to be set
     * @return CompoundTag - (from vanilla) nbt data to set
     * @throws CommandSyntaxException - (from vanilla) error occurs executing command
     */
    boolean bellows_setDataFiltered(CompoundTag nbt, String path) throws CommandSyntaxException;

    /**
     * Retrieves the entity associated with this EntityDataAccessor.
     * @return Entity - associated entity
     */
    Entity bellows_getContents();
}
