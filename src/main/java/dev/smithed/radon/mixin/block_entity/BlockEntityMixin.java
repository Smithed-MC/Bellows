package dev.smithed.radon.mixin.block_entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IEntityMixin, ICustomNBTMixin {

    @Shadow @Final BlockPos worldPosition;

    @Override
    public boolean writeCustomDataToNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(CompoundTag nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public CompoundTag saveWithoutIdFiltered(CompoundTag nbt, String path) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        final BlockEntity entity = ((BlockEntity) (Object) this);

        switch (topLevelNbt) {
            case "x" -> nbt.putInt("x", this.worldPosition.getX());
            case "y" -> nbt.putInt("y", this.worldPosition.getY());
            case "z" -> nbt.putInt("z", this.worldPosition.getZ());
            case "id" -> {
                ResourceLocation identifier = BlockEntityType.getKey(entity.getType());
                if (identifier == null) {
                    throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
                } else {
                    nbt.putString("id", identifier.toString());
                }
            }
            default -> {
                if (this.writeCustomDataToNbtFiltered(nbt, path, topLevelNbt))
                    return nbt;
                else
                    return null;
            }
        }
        return nbt;
    }

    @Override
    public boolean loadFiltered(CompoundTag nbt, String path) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        return this.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt);
    }
}