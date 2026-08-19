package net.smithed.bellows.mixin_interface;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityDataAccessorExtender {

    CompoundTag bellows_getDataFiltered(String path) throws CommandSyntaxException;
    boolean bellows_setDataFiltered(CompoundTag nbt, String path) throws CommandSyntaxException;
    Entity bellows_getContents();
}
