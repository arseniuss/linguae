package lv.id.arseniuss.linguae.app.db;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lv.id.arseniuss.linguae.tasks.Task;

public class ITaskDataTypeAdapter extends TypeAdapter<Task.ITaskData> {
    private final Gson gson;

    public ITaskDataTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Task.ITaskData value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        String json = gson.toJson(value);
        String base64 =
                Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        out.value(base64);
    }

    @Override
    public Task.ITaskData read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String base64 = in.nextString();
        byte[] decodedBytes = Base64.decode(base64, Base64.NO_WRAP);
        String json = new String(decodedBytes, StandardCharsets.UTF_8);

        return gson.fromJson(json, Task.ITaskData.class);
    }
}
