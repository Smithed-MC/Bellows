package dev.smithed.radon.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Transformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class TransformTagArgument implements ArgumentType<CombinedTransformation> {

    private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo=bar}");
    private static final Set<String> ACCEPTED_NBT = Set.of(
        "transformation",
        "start_interpolation",
        "interpolation_duration"
    );

    private TransformTagArgument() {}

    public static TransformTagArgument compoundTag() {
        return new TransformTagArgument();
    }

    public static <S> CombinedTransformation getCompoundTag(CommandContext<S> context, String name) {
        return context.getArgument(name, CombinedTransformation.class);
    }

    public CombinedTransformation parse(StringReader reader) throws CommandSyntaxException {
        CompoundTag nbt = TagParser.parseCompoundAsArgument(reader);

        Optional<Transformation> transformation = nbt.read("transformation", Transformation.EXTENDED_CODEC);
        int startInterpolation = nbt.getIntOr("start_interpolation", 0);
        Optional<Integer> interpolationDuration = nbt.getInt("interpolation_duration");

        if(transformation.isEmpty()) {
            throw new CommandSyntaxException(null, () -> "Invalid nbt args");
        }

        for(String key: nbt.keySet()) {
            if(!ACCEPTED_NBT.contains(key)) {
                throw new CommandSyntaxException(null, () -> "Invalid nbt args");
            }
        }

        return new CombinedTransformation(transformation.get(), startInterpolation, interpolationDuration);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
