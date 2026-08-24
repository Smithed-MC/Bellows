package net.smithed.bellows.mixin_interface.selector;


import java.util.Set;

public interface MinecraftServerExtender {

    /**
     * Gets a set of entity types in a specified entity type tag.
     * @param tag - entity type tag id
     * @return Set<String> - set of entity types in the entity type tag
     */
    Set<String> bellows_getEntityTagEntries(String tag);
}
