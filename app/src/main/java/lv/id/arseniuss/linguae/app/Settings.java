package lv.id.arseniuss.linguae.app;

import java.util.List;

import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;

public class Settings {
    public static boolean IgnoreMacrons = false;

    public static String AlphabetOrder = "";

    private static List<SettingEntity> _settingEntities = null;

    public static void Parse(List<SettingEntity> settings) {
        _settingEntities = settings;

        for (SettingEntity setting : settings) {
            if (setting.Key.equals(Constants.IgnoreMacronsKey)) {
                IgnoreMacrons = Boolean.parseBoolean(setting.Value);
            } else if (setting.Key.equals(Constants.AlphabetOrderKey)) {
                AlphabetOrder = setting.Value;
            }
        }
    }

    public static List<SettingEntity> Get() {
        return _settingEntities;
    }
}
