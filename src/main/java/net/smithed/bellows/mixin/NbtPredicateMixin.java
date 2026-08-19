package net.smithed.bellows.mixin;

import net.smithed.bellows.Bellows;
import net.smithed.bellows.mixin_interface.EntityExtender;
import net.smithed.bellows.utils.NBTUtils;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;

@Mixin(NbtPredicate.class)
public abstract class NbtPredicateMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private CompoundTag tag;

    @Shadow
    public abstract boolean matches(@Nullable Tag tag);

    /**
     * @author ImCoolYeah105
     * @reason overwrite get nbt function to add filter support
     */
    @Overwrite
    public boolean matches(Entity entity) {
        CompoundTag nbt = null;
        if(Bellows.CONFIG.nbtOptimizations && entity instanceof EntityExtender mixin) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                for (String str : NBTUtils.getTopLevelPaths(this.tag)) {
                    if(output.buildResult().contains(str)) {
                        continue;
                    }
                    // if any attempt to get a data element fails, mark output as a failure and break out of the loop
                    if(!bellows_getFilteredNbt(mixin, output, str)) {
                        output = null;
                        break;
                    }
                }
                if(output != null) {
                    nbt = output.buildResult();
                }
            }
        }

        if(nbt == null) {
            nbt = NbtPredicate.getEntityTagToCompare(entity);
        }

        boolean result = this.matches(nbt);
        Bellows.logDebugFormat("Predicate = %s, nbt = %s", result, nbt);
        return result;
    }

    @Unique
    private boolean bellows_getFilteredNbt(EntityExtender mixin, TagValueOutput output, String path) {
        if (mixin instanceof Player player && path.startsWith("SelectedItem") && NBTUtils.isPathSelectedItem(path)) {
            ItemStack selected = player.getInventory().getSelectedItem();
            if (!selected.isEmpty()) {
                output.store("SelectedItem", ItemStack.CODEC, selected);
            }
            return true;
        } else {
            return mixin.bellows_saveWithoutIdFiltered(output, path);
        }
    }
}
