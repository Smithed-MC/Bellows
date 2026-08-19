package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(EntityDataObject.class)
public class EntityDataObjectMixin implements IDataCommandObjectMixin {

    @Final @Shadow Entity entity;
    @Final @Shadow static SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION;

    private static final int MIN_ENTITY_NBT_SIZE = 19; //this is how many NBT tags the base entity class contains

    @Override
    public NbtCompound getNbtFiltered(String path) {
        NbtCompound nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && this.entity instanceof IEntityMixin mixin) {
            if (path.startsWith("{")) {
                nbtCompound = new NbtCompound();
                for (String str : NBTUtils.getTopLevelPaths(path)) {
                    nbtCompound = getFilteredNbt(mixin, nbtCompound, str);
                    if (nbtCompound == null)
                        break;
                }
            } else {
                nbtCompound = getFilteredNbt(mixin, new NbtCompound(), path);
            }
        }
        if (nbtCompound == null) {
            Radon.logDebugFormat("Failed to get filtered nbt '%s' for entity '%s'", path, this.entity.getClass());
            nbtCompound = NbtPredicate.entityToNbt(this.entity);
            Radon.logDebugFormat("Retrieved NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbtCompound);
        } else {
            Radon.logDebugFormat("Retrieved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbtCompound);
        }
        return nbtCompound;
    }

    private NbtCompound getFilteredNbt(IEntityMixin mixin, NbtCompound nbtCompound, String path) {
        if (this.entity instanceof PlayerEntity player && path.startsWith("SelectedItem")) {
            ItemStack itemStack = player.getInventory().getMainHandStack();
            if (!itemStack.isEmpty()) {
                nbtCompound.put("SelectedItem", itemStack.toNbt(player.getRegistryManager()));
            }
            return nbtCompound;
        } else {
            return mixin.writeNbtFiltered(nbtCompound, path);
        }
    }

    @Override
    public boolean setNbtFiltered(NbtCompound nbt, String path) throws CommandSyntaxException {
        if (this.entity instanceof PlayerEntity) {
            throw INVALID_ENTITY_EXCEPTION.create();
        } else {
            UUID uUID = this.entity.getUuid();
            if (this.entity instanceof IEntityMixin mixin && mixin.readNbtFiltered(nbt, path)) {
                Radon.logDebugFormat("Saved filtered NBT '%s' for entity '%s', data = %s", path, this.entity.getClass(), nbt);
                this.entity.setUuid(uUID);
                return true;
            }
            if(nbt.getSize() >= MIN_ENTITY_NBT_SIZE) {
                this.entity.readNbt(nbt);
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