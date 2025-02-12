package lv.id.arseniuss.linguae;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.db.entities.Config;

public class Configuration {
    private static final List<OnConfigChangedListener> _listeners = new ArrayList<>();
    private static Map<String, String> _configs;

    public static void Parse(List<Config> configs) {
        _configs = configs.stream().collect(Collectors.toMap(Config::GetKey, Config::GetValue));
        _listeners.forEach(OnConfigChangedListener::ConfigChanged);
    }

    public static String GetLanguageImageUrl() {
        if (_configs.containsKey("image-url")) return _configs.get("image-url");
        return null;
    }

    public static Bitmap GetLanguageImage() {
        if (_configs.containsKey("image")) {
            String base64 = _configs.get("image");
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        return null;
    }

    public static void AddConfigChangeLister(OnConfigChangedListener listener) {
        _listeners.add(listener);
    }

    public static void RemoveConfigChangeLister(OnConfigChangedListener listener) {
        _listeners.remove(listener);
    }

    public interface OnConfigChangedListener {
        void ConfigChanged();
    }
}
