package dev.smithed.radon.commands;

import com.mojang.math.Transformation;

import java.util.Optional;

public record CombinedTransformation(Transformation transformation, int startInterpolation, Optional<Integer> interpolationDuration) {}
