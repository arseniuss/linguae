package lv.id.arseniuss.linguae.app.data;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import lv.id.arseniuss.linguae.interfaces.IntValueSerializable;

public class IntValueEnumTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!rawType.isEnum() || !IntValueSerializable.class.isAssignableFrom(rawType)) {
            return null;
        }
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    // Serialize using the interface's getValue()
                    out.value(((IntValueSerializable) value).GetValue());
                }
            }

            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                int value = in.nextInt();
                return IntValueSerializable.FromValue((Class<T>)rawType, value);
            }
        };
    }
}
