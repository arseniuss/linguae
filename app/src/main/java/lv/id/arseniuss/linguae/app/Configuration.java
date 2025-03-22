package lv.id.arseniuss.linguae.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.app.db.entities.ConfigEntity;

public class Configuration {
    public static final String VERSION_KEY = "version";
    public static final String IMAGE_URL_KEY = "image-url";
    public static final String IMAGE_KEY = "image";
    public static final String AUTHOR_KEY = "author";
    public static final String CODE_KEY = "code";

    private static final List<OnConfigChangedListener> _listeners = new ArrayList<>();
    private static Map<String, String> _configs;

    public static void Parse(List<ConfigEntity> configs) {
        _configs = configs.stream()
                .collect(Collectors.toMap(ConfigEntity::GetKey, ConfigEntity::GetValue));
        _listeners.forEach(OnConfigChangedListener::ConfigChanged);
    }

    public static String GetLanguageVersion() {
        if (_configs.containsKey(VERSION_KEY)) return _configs.get(VERSION_KEY);
        return null;
    }

    public static String GetLanguageCode() {
        if (_configs.containsKey(CODE_KEY)) return _configs.get(CODE_KEY);
        return null;
    }

    public static String GetLanguageImageUrl() {
        if (_configs.containsKey(IMAGE_URL_KEY)) return _configs.get(IMAGE_URL_KEY);
        return null;
    }

    public static Bitmap GetLanguageImage() {
        if (_configs.containsKey(IMAGE_KEY)) {
            String base64 = _configs.get(IMAGE_KEY);
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

    public static String GetLanguageAuthor() {
        if (_configs.containsKey(AUTHOR_KEY)) return _configs.get(AUTHOR_KEY);
        return null;
    }

    public interface OnConfigChangedListener {
        void ConfigChanged();
    }
}
