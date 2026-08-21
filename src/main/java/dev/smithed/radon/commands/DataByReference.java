package dev.smithed.radon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.commands.data.StorageDataAccessor;

import java.util.List;
import java.util.function.BiConsumer;

public class DataByReference {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("data-link")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                DataCommands.decorateModification((parent, rest) -> parent
                    .then(Commands.literal("set").then(rest.create(DataByReference::linkTags)))
                )
            )
        );
    }

    public static ArgumentBuilder<CommandSourceStack, ?> decorateModification(final BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> nodeSupplier) {
        LiteralArgumentBuilder<CommandSourceStack> modify = Commands.literal("modify");

        DataCommands.DataProvider targetProvider = StorageDataAccessor.PROVIDER.apply("target");
        DataCommands.DataProvider sourceProvider = StorageDataAccessor.PROVIDER.apply("source");

        targetProvider.wrap(modify, (t) -> {
            ArgumentBuilder<CommandSourceStack, ?> targetPathNode = Commands.argument("targetPath", NbtPathArgument.nbtPath());

            nodeSupplier.accept(targetPathNode, (manipulator) -> sourceProvider.wrap(Commands.literal("from"), (s) ->
                    s.executes((c) -> manipulateData(c, targetProvider, manipulator, NbtTagArgument.getNbtTag(c, "value"))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((c) -> manipulateData(c, targetProvider, manipulator, NbtTagArgument.getNbtTag(c, "value"))))));

            nodeSupplier.accept(targetPathNode, (manipulator) -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag())
                    .executes((c) -> manipulateData(c, targetProvider, manipulator, NbtTagArgument.getNbtTag(c, "value")))));
            return t.then(targetPathNode);
        });

        return modify;
    }

    private static int manipulateData(final CommandContext<CommandSourceStack> context, final DataCommands.DataProvider targetProvider, final DataCommands.DataManipulator manipulator, final Tag source) throws CommandSyntaxException {
        DataAccessor target = targetProvider.access(context);
        NbtPathArgument.NbtPath targetPath = NbtPathArgument.getPath(context, "targetPath");
        CompoundTag targetData = target.getData();
        int result = manipulator.modify(context, targetData, targetPath, List.of(source));

        target.setData(targetData);
        context.getSource().sendSuccess(target::getModifiedSuccess, true);
        return result;
    }

    private static int linkTags(CommandContext<CommandSourceStack> context, CompoundTag target, NbtPathArgument.NbtPath targetPath, List<Tag> source) {
        if(source.isEmpty()) {
            return 0;
        }
        target.put(targetPath.asString(), source.getFirst());
        return 1;
    }
}
