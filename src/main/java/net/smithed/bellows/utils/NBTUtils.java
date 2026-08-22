package net.smithed.bellows.utils;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public class NBTUtils {

    /**
     * Gets the inventory slot from a nbt path.
     * @param nbt - path
     * @return int - Slot in nbt path, or -1 for no slot specified
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

    /**
     * Finds all top level nbt paths in a data merge tag from nbt string.
     * @param nbt - nbt string
     * @return String[] - top level paths
     */
    public static String[] getTopLevelPaths(String nbt) {
        nbt = nbt.substring(1,nbt.length()-1) + ",";
        return nbt.split(":(\\[(.*?)])*(\\{(.*?)})*(.*?),");
    }

    /**
     * Finds all top level nbt paths in a data merge tag from nbt compound.
     * @param nbt - nbt compound
     * @return String[] - top level paths
     */
    public static String[] getTopLevelPaths(CompoundTag nbt) {
        Set<String> set = nbt.keySet();
        String[] strings = new String[set.size()];
        return nbt.keySet().toArray(strings);
    }
}
