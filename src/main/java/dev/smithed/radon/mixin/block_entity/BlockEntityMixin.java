package dev.smithed.radon.mixin.block_entity;

import dev.smithed.radon.mixin_interface.ICustomNBTMixin;
import dev.smithed.radon.mixin_interface.IEntityMixin;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IEntityMixin, ICustomNBTMixin {

    @Shadow @Final BlockPos pos;

    @Override
    public boolean writeCustomDataToNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public boolean readCustomDataFromNbtFiltered(NbtCompound nbt, String path, String topLevelNbt) {
        return false;
    }

    @Override
    public NbtCompound writeNbtFiltered(NbtCompound nbt, String path) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        final BlockEntity entity = ((BlockEntity) (Object) this);

        switch (topLevelNbt) {
            case "x" -> nbt.putInt("x", this.pos.getX());
            case "y" -> nbt.putInt("y", this.pos.getY());
            case "z" -> nbt.putInt("z", this.pos.getZ());
            case "id" -> {
                Identifier identifier = BlockEntityType.getId(entity.getType());
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
    public boolean readNbtFiltered(NbtCompound nbt, String path) {
        String topLevelNbt = path.split("[\\[.{]")[0];
        return this.readCustomDataFromNbtFiltered(nbt, path, topLevelNbt);
    }
}