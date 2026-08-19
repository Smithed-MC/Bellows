package dev.smithed.radon.utils;

import com.mojang.math.Transformation;
import dev.smithed.radon.Bellows;
import dev.smithed.radon.mixin_interface.DisplayEntityExtender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class QuickActions {

    private static final Map<String, Function<CompoundTag, Consumer<Entity>>> QUICK_ACTIONS = Map.of(
        "transformation", tag -> {
            Optional<CompoundTag> transformation = tag.getCompound("transformation");
            if(transformation.isPresent()) {
                Optional<Vector3fc> translation = transformation.get().read("translation", ExtraCodecs.VECTOR3F);
                Optional<Quaternionfc> left_rotation = transformation.get().read("left_rotation", ExtraCodecs.QUATERNIONF);
                Optional<Vector3fc> scale = transformation.get().read("scale", ExtraCodecs.VECTOR3F);
                Optional<Quaternionfc> right_rotation = transformation.get().read("right_rotation", ExtraCodecs.QUATERNIONF);

                return transformation.<Consumer<Entity>>map(_ -> (Entity entity) -> {
                    if (entity instanceof Display display && entity instanceof DisplayEntityExtender extender) {
                        translation.ifPresent(extender::bellows_setTranslation);
                        left_rotation.ifPresent(extender::bellows_setLeftRotation);
                        scale.ifPresent(extender::bellows_setScale);
                        right_rotation.ifPresent(extender::bellows_setRightRotation);
                    }
                }).orElse(null);
            }

            Optional<ListTag> matrix = tag.getList("transformation");
            if(matrix.isPresent()) {
                Optional<Transformation> transform = tag.read("transformation", Transformation.EXTENDED_CODEC);

                return transform.<Consumer<Entity>>map(_ -> (Entity entity) -> {
                    if (entity instanceof DisplayEntityExtender extender) {
                        extender.bellows_setTranslation(transform.get().translation());
                        extender.bellows_setLeftRotation(transform.get().leftRotation());
                        extender.bellows_setScale(transform.get().scale());
                        extender.bellows_setRightRotation(transform.get().rightRotation());
                    }
                }).orElse(null);
            }

            return null;
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
                Bellows.logDebugFormat("Found quick action: %s", key);
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
