package net.smithed.bellows.mixin_interface;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface DisplayEntityExtender {

    void bellows_setTranslation(Vector3fc translation);
    void bellows_setLeftRotation(Quaternionfc translation);
    void bellows_setScale(Vector3fc scale);
    void bellows_setRightRotation(Quaternionfc translation);
}
