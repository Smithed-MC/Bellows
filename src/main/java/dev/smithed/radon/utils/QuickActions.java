package dev.smithed.radon.utils;

import com.mojang.math.Transformation;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.DisplayEntityExtender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class QuickActions {

    private static final Map<String, Function<CompoundTag, Consumer<Entity>>> QUICK_ACTIONS = Map.of(
            "transformation", tag -> {
                Optional<Transformation> transformation = tag.read("transformation", Transformation.EXTENDED_CODEC);
                return transformation.<Consumer<Entity>>map(value -> (Entity entity) -> {
                    if (entity instanceof Display display && entity instanceof DisplayEntityExtender extender && !extender.radon_hasTransformation(transformation.get())) {
                        display.setTransformation(value);
                        display.setTransformationInterpolationDelay(0);
                    }
                }).orElse(null);
            }
    );

    private final Set<String> quickActionTags;
    private final Set<Consumer<Entity>> quickActions;

    public QuickActions(CompoundTag tag) {
        quickActionTags = new HashSet<>();
        quickActions = computeQuickActions(tag);
    }

    private Set<Consumer<Entity>> computeQuickActions(CompoundTag tag) {
        Set<Consumer<Entity>> actions = new HashSet<>();
        for(String key: tag.keySet()) {
            Function<CompoundTag, Consumer<Entity>> function = QUICK_ACTIONS.get(key);
            if(function == null) {
                continue;
            }
            Consumer<Entity> action = function.apply(tag);
            if(action != null) {
                Radon.logDebugFormat("Found quick action: %s", key);
                quickActionTags.add(key);
                actions.add(action);
            }
        }
        return actions.isEmpty() ? null : actions;
    }

    public boolean hasQuickActions() {
        return !quickActionTags.isEmpty();
    }

    public Set<String> getQuickActionTags() {
        return quickActionTags;
    }

    public Set<Consumer<Entity>> getQuickActions() {
        return quickActions;
    }

    public void merge(QuickActions other) {
        quickActionTags.addAll(other.getQuickActionTags());
        quickActions.addAll(other.getQuickActions());
    }
}
