package net.smithed.bellows;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.smithed.bellows.mixin_interface.nbt.EntityExtender;
import org.slf4j.Logger;

import java.util.List;

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

    @GameTest
    public void test_entities(GameTestHelper context) {
        try {
            for (EntityType<? extends Entity> entityType : MODIFIED_ENTITIES) {
                test_entity(context, entityType);
            }
        } catch (Exception ex) {
            context.fail(ex.getMessage());
            return;
        }
        context.succeed();
    }

    private void test_entity(GameTestHelper context, EntityType<? extends Entity> entityType) {
        Entity entity = entityType == EntityTypes.PLAYER
                ? context.makeMockServerPlayer(GameType.CREATIVE)
                : context.spawn(entityType, getBoundedPos(context));
        EntityExtender entityExtender = (EntityExtender) entity;

        CompoundTag defaultTag;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(output);
            defaultTag = output.buildResult();

            defaultTag.forEach((key, tag) -> {
                TagValueOutput input = TagValueOutput.createWithContext(reporter, entity.registryAccess());
                assertTrue(entityExtender.bellows_saveWithoutIdFiltered(input, key), "Failed to load filtered data: " + key + ", " + entity.getClass());
                CompoundTag result = input.buildResult();
                assertTrue(result.get(key).equals(tag));
            });
        }
    }

    private static BlockPos getBoundedPos(GameTestHelper context) {
        return new BlockPos((int) context.getBounds().minX, (int) context.getBounds().minY, (int) context.getBounds().minZ);
    }

    private static void assertTrue(boolean value) {
        if(!value) {
            throw new AssertionError();
        }
    }

    private static void assertTrue(boolean value, String message) {
        if(!value) {
            throw new AssertionError(message);
        }
    }
}
