package lv.id.arseniuss.linguae.interfaces;

public interface IntValueSerializable {
    static <T> T FromValue(Class<T> enumClass, int value) {
        for (T constant : enumClass.getEnumConstants()) {

            IntValueSerializable e = (IntValueSerializable) constant;

            if (e.GetValue() == value) {
                return constant;
            }
        }
        throw new IllegalArgumentException(
                "No enum constant in " + enumClass.getSimpleName() + " with value " + value);
    }

    int GetValue();
}
