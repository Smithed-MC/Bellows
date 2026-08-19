package dev.smithed.radon.mixin_interface;

import com.mojang.math.Transformation;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface DisplayEntityExtender {

    boolean radon_hasTransformation(Transformation transformation);

    void radon_setTranslation(Vector3fc translation);
    void radon_setLeftRotation(Quaternionfc translation);
    void radon_setScale(Vector3fc scale);
    void radon_setRightRotation(Quaternionfc translation);
}
