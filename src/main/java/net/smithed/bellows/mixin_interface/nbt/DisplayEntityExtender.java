package net.smithed.bellows.mixin_interface.nbt;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface DisplayEntityExtender {

    /**
     * Directly sets a display entity's translation, without using a full Transformation object.
     * @param translation - translation to apply
     */
    void bellows_setTranslation(Vector3fc translation);

    /**
     * Directly sets a display entity's left rotation, without using a full Transformation object.
     * @param leftRotation - left rotation to apply
     */
    void bellows_setLeftRotation(Quaternionfc leftRotation);

    /**
     * Directly sets a display entity's scale, without using a full Transformation object.
     * @param scale - scale to apply
     */
    void bellows_setScale(Vector3fc scale);

    /**
     * Directly sets a display entity's right rotation, without using a full Transformation object.
     * @param rightRotation - right rotation to apply
     */
    void bellows_setRightRotation(Quaternionfc rightRotation);
}
