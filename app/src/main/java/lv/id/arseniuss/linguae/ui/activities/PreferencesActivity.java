package lv.id.arseniuss.linguae.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.Settings;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.db.entities.Setting;

public class PreferencesActivity extends AppCompatActivity {
    private MainDataAccess _mainDataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(getBaseContext()))
                    .commit();
        }

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String language = sharedPreferences.getString(Constants.PreferenceLanguageKey, "");

        _mainDataAccess =
                LanguageDatabase.GetInstance(getBaseContext(), language).GetMainDataAccess();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            Disposable d = _mainDataAccess.SaveSetting(Settings.Get())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::finish, error -> {
                        Utilities.PrintToastError(this, error);
                        finish();
                    });

            return true;
        } else if (R.id.item_clear_settings == item.getItemId()) {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            preferences.edit().clear().apply();
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final SharedPreferences _sharedPreferences;

        private String _rootKey;

        public SettingsFragment(Context context) {
            _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            _rootKey = rootKey;

            setPreferencesFromResource(R.xml.preferences, rootKey);

            load();
            setup();
        }

        private void load() {
            Preference languagePreferences =
                    findPreference(getString(R.string.PreferencesLanguageKey));

            if (languagePreferences != null) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) languagePreferences;
                List<Setting> settings = Settings.Get();

                if (settings != null) {
                    for (Setting setting : settings) {
                        Preference preference = null;
                        String settingKey = "PREF_LANG_" + setting.Key;

                        switch (setting.Type) {
                            case Boolean:
                                preference = new SwitchPreference(getContext());

                                _sharedPreferences.edit()
                                        .putBoolean(settingKey, Boolean.parseBoolean(setting.Value))
                                        .apply();
                                preference.setOnPreferenceChangeListener(
                                        (preference1, newValue) -> {
                                            setting.Value = newValue.toString();

                                            return true;
                                        });
                                break;
                        }

                        if (preference != null) {
                            preference.setKey(settingKey);
                            preference.setTitle(setting.Description);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                }
            }
        }

        private void setup() {
            String language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
            Preference langPreference = findPreference(Constants.PreferenceLanguageKey);

            assert langPreference != null;

            if (!language.isEmpty()) {
                langPreference.setSummary(language);
            }

            langPreference.setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(getContext(), StartActivity.class);

                i.putExtra(StartActivity.RESTART, true);

                startActivity(i);
                return false;
            });
        }
    }
}