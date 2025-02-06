package lv.id.arseniuss.linguae;

import java.util.List;

import lv.id.arseniuss.linguae.db.entities.Setting;

public class Settings {
    public static boolean IgnoreMacrons = false;

    private static List<Setting> _settings = null;

    public static void Parse(List<Setting> settings) {
        _settings = settings;

        for (Setting setting : settings) {
            if (setting.Key.equals(Constants.IgnoreMacronsKey)) {
                IgnoreMacrons = Boolean.parseBoolean(setting.Value);
            }
        }
    }

    public static List<Setting> Get() {
        return _settings;
    }
}
