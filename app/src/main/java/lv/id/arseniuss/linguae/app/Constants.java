package lv.id.arseniuss.linguae.app;

import android.content.Context;

public class Constants {
    public static String PreferenceLanguageKey;
    public static String PreferenceRepositoriesKey;
    public static String PreferenceLanguageUrlKey;
    public static String PreferenceNoKeyboardKey;
    public static String PreferenceTaskCountKey;
    public static String IgnoreMacronsKey;
    public static String PreferenceSaveImagesKey;
    public static String ChooseOptionCountKey;

    public static void Init(Context context) {
        PreferenceLanguageKey = context.getString(R.string.PreferenceLanguageKey);
        PreferenceRepositoriesKey = context.getString(R.string.PreferenceRepositoriesKey);
        PreferenceLanguageUrlKey = context.getString(R.string.PreferenceLanguageUrlKey);
        PreferenceNoKeyboardKey = context.getString(R.string.PreferenceNoKeyboardKey);
        PreferenceTaskCountKey = context.getString(R.string.PreferenceTaskCountKey);
        PreferenceSaveImagesKey = context.getString(R.string.PreferenceSaveImageKey);

        IgnoreMacronsKey = context.getString(R.string.IgnoreMacronsKey);
        ChooseOptionCountKey = context.getString(R.string.ChooseOptionCountKey);
    }
}
