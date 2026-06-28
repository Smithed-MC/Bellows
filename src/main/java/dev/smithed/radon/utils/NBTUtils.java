package dev.smithed.radon.utils;

import java.util.Set;
import net.minecraft.nbt.CompoundTag;

public class NBTUtils {

    /**
     * Gets the inventory slot from a nbt path.
     * @param nbt path
     * @return Slot in nbt path, or -1 for no slot specified
     */
    public static int getSlot(String nbt) {
        //isolate first bracket pair, ie. Items"[]"
        int sIndex = nbt.indexOf('[');
        int eIndex = nbt.indexOf(']');
        if(sIndex == -1 || eIndex == -1)
            return -1;
        else
            sIndex += 1;

        //isolate insides of []
        String slot = nbt.substring(sIndex, eIndex);

        //find Slot:#b
        int slotIndex = slot.indexOf("Slot:");
        int bIndex = slot.indexOf('b', slotIndex);

        //attempt to parse #
        if(slotIndex == -1 || bIndex == -1)
            return -1;
        try {
            return Integer.parseInt(slot.substring(slotIndex + 5, bIndex));
        } catch(NumberFormatException ignored) {}
        return -1;
    }

    public static String[] getTopLevelPaths(String nbt) {
        nbt = nbt.substring(1,nbt.length()-1) + ",";
        return nbt.split(":(\\[(.*?)\\])*(\\{(.*?)\\})*(.*?),");
    }

    public static String[] getTopLevelPaths(CompoundTag nbt) {
        Set<String> set = nbt.keySet();
        String[] strings = new String[set.size()];
        return nbt.keySet().toArray(strings);
    }

    public static String translationToTypeName(String name) {
        String[] split = name.split("\\.");
        if(split.length > 2)
            return split[1] + ":" + split[2];
        return "";
    }
}
