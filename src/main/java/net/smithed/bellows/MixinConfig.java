package net.smithed.bellows;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfig implements IMixinConfigPlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad(String mixinPackage) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("net.smithed.bellows.mixin.selector.moonrise")) {
            return isMoonriseLoaded();
        }
        return true;
    }

    /**
     * Returns true if the Moonrise mod is loaded.
     * @return boolean - true if Moonrise is loaded
     */
    private boolean isMoonriseLoaded() {
        try {
            Class.forName("ca.spottedleaf.moonrise.common.util.MoonriseCommon", false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMixins() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
