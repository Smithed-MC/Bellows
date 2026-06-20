package dev.smithed.radon.mixin;

import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

@Mixin(NbtPredicate.class)
public class NbtPredicateMixin {

    @Shadow @Final private CompoundTag tag;

    /**
     * @author ImCoolYeah105
     * @reason overwrite get nbt function to add filter support
     */
    @Overwrite
    public boolean matches(Entity entity) {
        NbtPredicate predicate = ((NbtPredicate)(Object)this);

        CompoundTag nbt = null;
        if(Radon.CONFIG.nbtOptimizations && entity instanceof IEntityMixin mixin) {
            nbt = new CompoundTag();
            String[] topLevelNbt = NBTUtils.getTopLevelPaths(this.tag);
            for(String topNbt: topLevelNbt) {
                if (entity instanceof ServerPlayer player && topNbt.equals("SelectedItem")) {
                    ItemStack itemStack = player.getInventory().getSelected();
                    if (!itemStack.isEmpty()) {
                        nbt.put("SelectedItem", itemStack.save(entity.registryAccess()));
                    }
                } else {
                    nbt = mixin.saveWithoutIdFiltered(nbt, topNbt);
                    if (nbt == null)
                        break;
                }
            }
        }

        if(nbt == null)
            nbt = NbtPredicate.getEntityTagToCompare(entity);
        boolean result = predicate.matches(nbt);
        Radon.logDebugFormat("Predicate = %s, nbt = %s", result, nbt);
        return result;
    }
}
