package dev.smithed.radon.mixin_interface;


import java.util.Set;

public interface IMinecraftServerExtender {

    Set<String> getEntityTagEntries(String tag);

}
