package dev.smithed.radon.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.smithed.radon.Radon;
import dev.smithed.radon.mixin_interface.IDataCommandObjectMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import dev.smithed.radon.utils.NBTUtils;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(EntityDataObject.class)
public class EntityDataObjectMixin implements IDataCommandObjectMixin {

    @Final
    @Shadow
    Entity entity;
    @Final
    @Shadow
    static SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION;

    @Override
    public NbtCompound getNbtFiltered(String path) {
        NbtCompound nbtCompound = null;
        if (Radon.CONFIG.nbtOptimizations && this.entity instanceof IEntityMixin mixin) {
            if (path.startsWith("{")) {
                nbtCompound = new NbtCompound();
                for (String str : NBTUtils.getTopLevelPaths(path)) {
                    nbtCompound = getFilteredNbt(mixin, nbtCompound, str, this.entity.getRegistryManager());
                    if (nbtCompound == null)
                        break;
                }
            } else {
                nbtCompound = getFilteredNbt(mixin, new NbtCompound(), path, this.entity.getRegistryManager());
            }
        }
        if (nbtCompound == null) {
            Radon.logDebugFormat("Failed to get NBT data at %s for %s", path, this.entity.getClass());
            nbtCompound = NbtPredicate.entityToNbt(this.entity);
        }
        Radon.logDebugFormat("Retrieved NBT for %s -> %s", this.entity.getClass(), nbtCompound);
        return nbtCompound;
    }

    private NbtCompound getFilteredNbt(IEntityMixin mixin, NbtCompound nbtCompound, String path, RegistryWrapper.WrapperLookup registries) {
        if (this.entity instanceof PlayerEntity player && path.startsWith("SelectedItem")) {
            ItemStack itemStack = player.getInventory().getMainHandStack();
            if (!itemStack.isEmpty())
                nbtCompound.put("SelectedItem", itemStack.toNbt(registries, new NbtCompound()));
            return nbtCompound;
        } else {
            return mixin.writeNbtFiltered(nbtCompound, path, registries);
        }
    }

    @Override
    public boolean setNbtFiltered(NbtCompound nbt, String path) throws CommandSyntaxException {
        if (this.entity instanceof PlayerEntity) {
            throw INVALID_ENTITY_EXCEPTION.create();
        } else {
            UUID uUID = this.entity.getUuid();
            if (this.entity instanceof IEntityMixin mixin && mixin.readNbtFiltered(nbt, path, this.entity.getRegistryManager())) {
                if (Radon.CONFIG.debug)
                    Radon.logDebugFormat("Set NBT for %s -> %s", this.entity.getClass(), nbt);
                this.entity.setUuid(uUID);
                return true;
            } else {
                if (Radon.CONFIG.debug)
                    Radon.logDebugFormat("Failed to save NBT data %s at %s for %s", nbt, path, this.entity.getClass());
                return false;
            }
        }
    }
}