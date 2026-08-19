package dev.smithed.radon.mixin_interface;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface EntityDataAccessorExtender {

    CompoundTag radon_getDataFiltered(String path) throws CommandSyntaxException;

    boolean radon_setDataFiltered(CompoundTag nbt, String path) throws CommandSyntaxException;

    Entity radon_getContents();
}
