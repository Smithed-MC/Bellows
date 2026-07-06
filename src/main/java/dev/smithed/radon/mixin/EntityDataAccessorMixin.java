package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataAccessorMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityDataAccessor.class)
public class EntityDataAccessorMixin implements IDataAccessorMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_NO_PLAYERS;
    @Shadow @Final private Entity entity;

    @Unique
    private static final int MIN_ENTITY_NBT_SIZE = 10; //this is how many NBT tags the base entity class contains

    @Override
    public CompoundTag radon_getDataFiltered(String path) {
        CompoundTag nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && this.entity instanceof IEntityMixin mixin) {
            // if path has multiple top level access ("{a:1,b:2,c:3}"), then split them and attempt to get each individually
            if (path.startsWith("{")) {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                    for (String str : NBTUtils.getTopLevelPaths(path)) {
                        if(output.buildResult().contains(str)) {
                            continue;
                        }
                        // if any attempt to get a data element fails, mark output as a failure and break out of the loop
                        if(!radon_getFilteredNbt(mixin, output, str)) {
                            output = null;
                            break;
                        }
                    }
                    if(output != null) {
                        nbtCompound = output.buildResult();
                    }
                }
            } else {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                    if(radon_getFilteredNbt(mixin, output, path)) {
                        nbtCompound = output.buildResult();
                    }
                }
            }
        }

        // if getting the filtered data failed, try using the normal method
        if (nbtCompound == null) {
            nbtCompound = NbtPredicate.getEntityTagToCompare(this.entity);
            Radon.logDebugFormat("Failed to get filtered nbt '%s' for entity '%s': falling back to default, data=", path, this.entity.getClass(), nbtCompound);
        } else {
            Radon.logDebugFormat("Retrieved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbtCompound);
        }
        return nbtCompound;
    }

    @Unique
    private boolean radon_getFilteredNbt(IEntityMixin mixin, TagValueOutput output, String path) {
        if (entity instanceof Player player && path.startsWith("SelectedItem")) {
            ItemStack selected = player.getInventory().getSelectedItem();
            if (!selected.isEmpty()) {
                output.store("SelectedItem", ItemStack.CODEC, selected);
            }
            return true;
        } else {
            return mixin.radon_saveWithoutIdFiltered(output, path);
        }
    }

    @Override
    public boolean radon_setDataFiltered(CompoundTag tag, String path) throws CommandSyntaxException {
        if (this.entity instanceof Player) {
            throw ERROR_NO_PLAYERS.create();
        } else {
            UUID uUID = this.entity.getUUID();

            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.entity.problemPath(), LOGGER)) {
                // attempt to save filtered data
                if (this.entity instanceof IEntityMixin mixin && mixin.radon_loadFiltered(TagValueInput.create(reporter, this.entity.registryAccess(), tag), path)) {
                    Radon.logDebugFormat("Saved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), tag);
                    this.entity.setUUID(uUID);
                    return true;
                }
                // if attempt to save filtered data failed and enough data exists to attempt a full save, do so. Otherwise, fail.
                if (tag.size() >= MIN_ENTITY_NBT_SIZE) {
                    this.entity.load(TagValueInput.create(reporter, this.entity.registryAccess(), tag));
                    Radon.logDebugFormat("Saved NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), tag);
                } else {
                    Radon.logDebugFormat("Failed to save NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), tag);
                }
            }
            return false;
        }
    }

    @Override
    public Entity radon_getContents() {
        return this.entity;
    }
}