package lv.id.arseniuss.linguae;

import android.content.Context;

public class Constants {
    public static String PreferenceLanguageKey;
    public static String PreferencePortalsKey;
    public static String PreferenceLanguageUrlKey;
    public static String PreferenceNoKeyboardKey;
    public static String PreferenceTaskCountKey;
    public static String IgnoreMacronsKey;
    public static String PreferenceSaveImagesKey;
    public static String ChooseOptionCountKey;

    public static void Init(Context context) {
        PreferenceLanguageKey = context.getString(R.string.PreferenceLanguageKey);
        PreferencePortalsKey = context.getString(R.string.PreferencePortalsKey);
        PreferenceLanguageUrlKey = context.getString(R.string.PreferenceLanguageUrlKey);
        PreferenceNoKeyboardKey = context.getString(R.string.PreferenceNoKeyboardKey);
        PreferenceTaskCountKey = context.getString(R.string.PreferenceTaskCountKey);
        PreferenceSaveImagesKey = context.getString(R.string.PreferenceSaveImageKey);

        IgnoreMacronsKey = context.getString(R.string.IgnoreMacronsKey);
        ChooseOptionCountKey = context.getString(R.string.ChooseOptionCountKey);
    }
}
