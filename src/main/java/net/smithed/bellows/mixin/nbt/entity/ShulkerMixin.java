package net.smithed.bellows.mixin.nbt.entity;

import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Shulker.class)
public abstract class ShulkerMixin extends MobMixin {

    @Shadow @Final protected static EntityDataAccessor<Byte> DATA_PEEK_ID;
    @Shadow @Final protected static EntityDataAccessor<Byte> DATA_COLOR_ID;
    @Shadow @Final private static Direction DEFAULT_ATTACH_FACE;

    @Shadow public Direction getAttachFace() { return null; }
    @Shadow private void setAttachFace(final Direction attachmentDirection) {}

    @Override
    public boolean bellows_addAdditionalSaveDataFiltered(ValueOutput output, String path, String topLevelNbt) {
        if (super.bellows_addAdditionalSaveDataFiltered(output, path, topLevelNbt)) {
            return true;
        }

        switch (topLevelNbt) {
            case "AttachFace" -> output.store("AttachFace", Direction.LEGACY_ID_CODEC, this.getAttachFace());
            case "Peek" -> output.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
            case "Color" -> output.putByte("Color", this.entityData.get(DATA_COLOR_ID));
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bellows_readAdditionalSaveDataFiltered(ValueInput input, String path, String topLevelNbt) {
        if (super.bellows_readAdditionalSaveDataFiltered(input, path, topLevelNbt)) {
            return true;
        }

        switch (topLevelNbt) {
            case "AttachFace" -> this.setAttachFace(input.read("AttachFace", Direction.LEGACY_ID_CODEC).orElse(DEFAULT_ATTACH_FACE));
            case "Peek" -> this.entityData.set(DATA_PEEK_ID, input.getByteOr("Peek", (byte)0));
            case "Color" -> this.entityData.set(DATA_COLOR_ID, input.getByteOr("Color", (byte)16));
            default -> {
                return false;
            }
        }
        return true;
    }
}
