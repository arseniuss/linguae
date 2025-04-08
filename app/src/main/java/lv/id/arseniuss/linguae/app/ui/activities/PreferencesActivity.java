package lv.id.arseniuss.linguae.app.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Settings;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.MainDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;
import lv.id.arseniuss.linguae.app.entities.ItemLanguageRepo;

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
        String language = sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");

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
            new MaterialAlertDialogBuilder(this).setMessage(R.string.MessageClearSettings)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        SharedPreferences preferences =
                                PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                        preferences.edit().clear().apply();

                        Intent i = new Intent(this, InitialActivity.class);

                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final int REPO_EDIT = 624;
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
                List<SettingEntity> settings = Settings.Get();

                if (settings != null) {
                    for (SettingEntity setting : settings) {
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
            setupLocalePreference();
            setupRepositoriesPreference();
            setupRepositoryLanguagePreference();
        }

        private void setupRepositoriesPreference() {
            Preference preference = findPreference(getString(R.string.PreferenceRepositoriesKey));
            String repositories =
                    _sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");

            assert preference != null;

            if (!repositories.isEmpty()) {
                List<ItemLanguageRepo> repos =
                        Utilities.UnpackList(repositories, ItemLanguageRepo.class);

                preference.setSummary(
                        getResources().getQuantityString(R.plurals.RepositoryCount, repos.size(),
                                repos.size()));
            }

            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(getContext(), RepoEditActivity.class);
                String json = _sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");

                i.putExtra(RepoEditActivity.DATA_ARRAY_JSON, json);

                startActivityForResult(i, REPO_EDIT);
                return false;
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REPO_EDIT && resultCode == RESULT_OK) {
                assert data != null;
                String result = data.getStringExtra(RepoEditActivity.DATA_ARRAY_JSON);

                _sharedPreferences.edit()
                        .putString(Constants.PreferenceRepositoriesKey, result)
                        .apply();

                setupRepositoriesPreference();
            }
        }

        private void setupRepositoryLanguagePreference() {
            Preference preference =
                    findPreference(getString(R.string.PreferenceRepositoryLanguageKey));
            String repository = _sharedPreferences.getString(Constants.PreferenceRepositoryKey, "");
            String language = _sharedPreferences.getString(Constants.PreferenceLanguageNameKey, "");

            assert preference != null;

            if (!repository.isEmpty() && !language.isEmpty()) {
                preference.setSummary(repository + "/" + language);
            }

            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(getContext(), InitialActivity.class);

                i.putExtra(InitialActivity.ACTION, InitialActivity.CHANGE_REPOSITORY);

                startActivity(i);

                return false;
            });
        }

        private void setupLocalePreference() {
            String locale = _sharedPreferences.getString(Constants.PreferenceLocaleNameKey, "");
            Preference localePreference = findPreference(Constants.PreferenceLocaleCodeKey);

            assert localePreference != null;

            if (!locale.isEmpty()) {
                localePreference.setSummary(locale);
            }

            localePreference.setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(getContext(), InitialActivity.class);

                i.putExtra(InitialActivity.ACTION, InitialActivity.CHANGE_DISPLAY_LANGUAGE);

                startActivity(i);
                return false;
            });
        }
    }
}