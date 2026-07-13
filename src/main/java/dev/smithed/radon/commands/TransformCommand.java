package dev.smithed.radon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.math.Transformation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class TransformCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("transform")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("targets", EntityArgument.entities())
                .then(Commands.argument("transformation", TransformTagArgument.compoundTag())
                .executes(command -> transform(command.getSource(), EntityArgument.getEntities(command, "targets"), TransformTagArgument.getCompoundTag(command, "transformation")))))
        );
    }

    private static int transform(final CommandSourceStack source, final Collection<? extends Entity> victims, final CombinedTransformation transformation) {
        int count = 0;
        for(Entity entity : victims) {
            if(entity instanceof Display display) {
                count++;
                display.setTransformation(transformation.transformation());
                display.setTransformationInterpolationDelay(transformation.startInterpolation());
                if(transformation.interpolationDuration().isPresent()) {
                    display.setTransformationInterpolationDuration(transformation.interpolationDuration().get());
                }
            }
        }
        if(count > 0) {
            final int finalCount = count;
            source.sendSuccess(() -> Component.literal("Updated " + finalCount + " display entities"), true);
        } else {
            source.sendFailure(Component.literal("Must target at least 1 display entity"));
        }
        return count;
    }
}
