package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataAccessorMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.utils.NBTUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(net.minecraft.server.commands.data.EntityDataAccessor.class)
public class EntityDataAccessor implements IDataAccessorMixin {

    @Final @Shadow Entity entity;
    @Final @Shadow static SimpleCommandExceptionType ERROR_NO_PLAYERS;

    private static final int MIN_ENTITY_NBT_SIZE = 19; //this is how many NBT tags the base entity class contains

    @Override
    public CompoundTag getNbtFiltered(String path) {
        CompoundTag nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && this.entity instanceof IEntityMixin mixin) {
            if (path.startsWith("{")) {
                nbtCompound = new CompoundTag();
                for (String str : NBTUtils.getTopLevelPaths(path)) {
                    nbtCompound = getFilteredNbt(mixin, nbtCompound, str);
                    if (nbtCompound == null)
                        break;
                }
            } else {
                nbtCompound = getFilteredNbt(mixin, new CompoundTag(), path);
            }
        }
        if (nbtCompound == null) {
            Radon.logDebugFormat("Failed to get filtered nbt '%s' for entity '%s'", path, this.entity.getClass());
            nbtCompound = NbtPredicate.getEntityTagToCompare(this.entity);
            Radon.logDebugFormat("Retrieved NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbtCompound);
        } else {
            Radon.logDebugFormat("Retrieved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbtCompound);
        }
        return nbtCompound;
    }

    private CompoundTag getFilteredNbt(IEntityMixin mixin, CompoundTag nbtCompound, String path) {
        if (this.entity instanceof Player player && path.startsWith("SelectedItem")) {
            ItemStack itemStack = player.getInventory().getSelected();
            if (!itemStack.isEmpty()) {
                nbtCompound.put("SelectedItem", itemStack.save(player.registryAccess()));
            }
            return nbtCompound;
        } else {
            return mixin.saveWithoutIdFiltered(nbtCompound, path);
        }
    }

    @Override
    public boolean setNbtFiltered(CompoundTag nbt, String path) throws CommandSyntaxException {
        if (this.entity instanceof Player) {
            throw ERROR_NO_PLAYERS.create();
        } else {
            UUID uUID = this.entity.getUUID();
            if (this.entity instanceof IEntityMixin mixin && mixin.loadFiltered(nbt, path)) {
                Radon.logDebugFormat("Saved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbt);
                this.entity.setUUID(uUID);
                return true;
            }
            if(nbt.size() >= MIN_ENTITY_NBT_SIZE) {
                this.entity.load(nbt);
                Radon.logDebugFormat("Saved NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbt);
            } else {
                Radon.logDebugFormat("Failed to save NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbt);
            }
            return false;
        }
    }

    @Override
    public Object getContents() {
        return this.entity;
    }
}