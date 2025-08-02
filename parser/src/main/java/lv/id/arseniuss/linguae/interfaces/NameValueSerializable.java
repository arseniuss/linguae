package lv.id.arseniuss.linguae.interfaces;

import java.util.Objects;

public interface NameValueSerializable {
    static <T extends Enum<T> & NameValueSerializable> T FromName(Class<T> enumClass, String value) {
        for (T constant : enumClass.getEnumConstants()) {

            if (Objects.equals(constant.GetName(), value)) {
                return constant;
            }
        }

        return null;
    }

    String GetName();
}
