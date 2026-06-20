package dev.smithed.radon.mixin_interface;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;

public interface IDataAccessorMixin {

    CompoundTag getNbtFiltered(String path) throws CommandSyntaxException;
    boolean setNbtFiltered(CompoundTag nbt, String path) throws CommandSyntaxException;

    Object getContents();

}
