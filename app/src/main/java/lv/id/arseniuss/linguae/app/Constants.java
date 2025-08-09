package lv.id.arseniuss.linguae.app;

import android.content.Context;

public class Constants {
    public static String PreferenceLocaleCodeKey;
    public static String PreferenceLocaleNameKey;
    public static String PreferenceDefaultLocaleCode;


    public static String PreferenceLanguageCodeKey;
    public static String PreferenceLanguageNameKey;

    public static String PreferenceRepositoriesKey;
    public static String PreferenceRepositoryKey;
    public static String PreferenceLanguageUrlKey;
    public static String PreferenceNoKeyboardKey;
    public static String PreferenceTaskCountKey;
    public static String IgnoreMacronsKey;
    public static String PreferenceSaveImagesKey;
    public static String ChooseOptionCountKey;

    public static String PreferenceStrictTaskCount;

    public static void Init(Context context) {
        PreferenceLocaleCodeKey = context.getString(R.string.PreferenceLocaleCodeKey);
        PreferenceLocaleNameKey = context.getString(R.string.PreferenceLocaleNameKey);
        PreferenceDefaultLocaleCode = context.getString(R.string.PreferenceDefaultLocaleCodeKey);

        PreferenceLanguageCodeKey = context.getString(R.string.PreferenceLanguageCodeKey);
        PreferenceLanguageNameKey = context.getString(R.string.PreferenceLanguageNameKey);

        PreferenceRepositoriesKey = context.getString(R.string.PreferenceRepositoriesKey);
        PreferenceRepositoryKey = context.getString(R.string.PreferenceRepositoryKey);
        PreferenceLanguageUrlKey = context.getString(R.string.PreferenceLanguageUrlKey);
        PreferenceNoKeyboardKey = context.getString(R.string.PreferenceNoKeyboardKey);
        PreferenceTaskCountKey = context.getString(R.string.PreferenceTaskCountKey);
        PreferenceSaveImagesKey = context.getString(R.string.PreferenceSaveImageKey);

        IgnoreMacronsKey = context.getString(R.string.IgnoreMacronsKey);
        ChooseOptionCountKey = context.getString(R.string.ChooseOptionCountKey);
        PreferenceStrictTaskCount = context.getString(R.string.PreferenceStrictTaskCount);
    }
}
