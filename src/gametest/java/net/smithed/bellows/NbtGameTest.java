package net.smithed.bellows;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

public class NbtGameTest {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<EntityType<? extends Entity>> MODIFIED_ENTITIES = List.of(
            EntityTypes.COPPER_GOLEM,
            EntityTypes.ITEM_DISPLAY,
            EntityTypes.TEXT_DISPLAY,
            EntityTypes.ITEM,
            EntityTypes.MANNEQUIN,
            EntityTypes.PLAYER,
            EntityTypes.SHULKER,
            EntityTypes.VILLAGER,
            EntityTypes.WANDERING_TRADER
    );

    private static final Set<String> EXCLUDE_TAGS = Set.of(
            "see_through",          // grouped text display values
            "default_background",
            "alignment",
            "VillagerDataFinalized" // villager finalized fields needs to be 1b
    );

    @GameTest
    public void test_entities(GameTestHelper context) {
        for (EntityType<? extends Entity> entityType : MODIFIED_ENTITIES) {
            test_entity(context, entityType);
        }
        context.succeed();
    }

    private void test_entity(GameTestHelper context, EntityType<? extends Entity> entityType) {
        Entity entity = entityType == EntityTypes.PLAYER
                ? context.makeMockServerPlayer(GameType.CREATIVE)
                : context.spawn(entityType, getBoundedPos(context));
        EntityExtender entityExtender = (EntityExtender) entity;

        if(entity instanceof Mob mob) {
            mob.persistenceRequired = false;
            mob.setCanPickUpLoot(false);
        }

        CompoundTag defaultTag;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(output);
            defaultTag = output.buildResult();

            defaultTag.forEach((key, tag) -> {
                // Test saving nbt data
                TagValueOutput output2 = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                assertTrue(entityExtender.bellows_saveWithoutIdFiltered(output2, key), "Failed to save filtered data: " + key + ", " + entity.getClass());
                CompoundTag result = output2.buildResult();
                assertTrue(result.get(key).equals(tag), "Loaded value did not match original value: " + key + ", " + entity.getClass() + ":" + tag + ":" + result.get(key));

                if(EXCLUDE_TAGS.contains(key) || entityType == EntityTypes.PLAYER) {
                    return;
                }

                CompoundTag newTag = modifyTagSlightly(key, tag);
                if(newTag.isEmpty()) {
                    return;
                }

                // test loading nbt data
                ValueInput input = TagValueInput.create(reporter, entity.registryAccess(), newTag);
                assertTrue(entityExtender.bellows_loadFiltered(input, key), "Failed to load filtered data: " + key + ", " + entity.getClass());

                TagValueOutput output3 = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                assertTrue(entityExtender.bellows_saveWithoutIdFiltered(output3, key), "Failed to save filtered data: " + key + ", " + entity.getClass());
                CompoundTag modifiedResult = output3.buildResult();
                assertTrue(modifiedResult.get(key).equals(newTag.get(key)), "Value did not change after loading: " + key + ":" + entity.getClass() + ":" + newTag + ":" + modifiedResult.get(key));

                // test removing nbt data
                CompoundTag emptyTag = new CompoundTag();
                ValueInput input2 = TagValueInput.create(reporter, entity.registryAccess(), emptyTag);
                assertTrue(entityExtender.bellows_loadFiltered(input2, key), "Failed to load filtered data: " + key + ", " + entity.getClass());

                TagValueOutput output4 = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                assertTrue(entityExtender.bellows_saveWithoutIdFiltered(output4, key), "Failed to save filtered data: " + key + ", " + entity.getClass());
                CompoundTag result2 = output4.buildResult();
                assertTrue(result2.get(key).equals(tag), "Value was not removed: " + key + ":" + entity.getClass() + ":" + tag + ":" + modifiedResult.get(key));
            });
        }
    }

    private static CompoundTag modifyTagSlightly(String key, Tag tag) {
        CompoundTag newTag = new CompoundTag();

        switch (tag) {
            case ByteTag castTag -> newTag.putByte(key, flip(castTag.value()));
            case ShortTag castTag -> newTag.putShort(key, flip(castTag.value()));
            case IntTag castTag -> newTag.putInt(key, flip(castTag.value()));
            case LongTag castTag -> newTag.putLong(key, flip(castTag.value()));
            case FloatTag castTag -> newTag.putFloat(key, shift(castTag.value()));
            case DoubleTag castTag -> newTag.putDouble(key, shift(castTag.value() + 1f));
            default -> {}
        }

        return newTag;
    }

    private static byte flip(byte b) {
        return b == 0 ? (byte) 1 : (byte) 0;
    }

    private static short flip(short s) {
        return s == 0 ? (short) 1 : (short) 0;
    }

    private static int flip(int s) {
        return s == 0 ?  1 : 0;
    }

    private static long flip(long s) {
        return s == 0L ?  1L : 0L;
    }

    private static float shift(float f) {
        return f == 0.0f ? 1.0f : f - 1.0f;
    }

    private static double shift(double f) {
        return f == 0.0 ? 1.0 : f - 1.0;
    }

    private static BlockPos getBoundedPos(GameTestHelper context) {
        return new BlockPos((int) context.getBounds().minX, (int) context.getBounds().minY, (int) context.getBounds().minZ);
    }

    private static void assertTrue(boolean value, String message) {
        if(!value) {
            throw new AssertionError(message);
        }
    }
}
