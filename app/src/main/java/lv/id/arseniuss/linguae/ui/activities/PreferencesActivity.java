package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.R;

public class PreferencesActivity extends AppCompatActivity {

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
                    .replace(R.id.settings,
                            new SettingsFragment(PreferenceManager.getDefaultSharedPreferences(getBaseContext())))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            this.finish();
            return true;
        }
        else if (R.id.item_clear_settings == item.getItemId()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            preferences.edit().clear().apply();
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final SharedPreferences _sharedPreferences;
        private String _rootKey;

        public SettingsFragment(SharedPreferences shaSharedPreferences) { _sharedPreferences = shaSharedPreferences; }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            _rootKey = rootKey;

            setPreferencesFromResource(R.xml.preferences, rootKey);

            setup();
        }

        private void setup() {
            String languageKey = getString(R.string.PreferenceLanguageKey);
            String language = _sharedPreferences.getString(languageKey, "");

            Preference langPreference = findPreference(languageKey);

            assert langPreference != null;

            if (!language.isEmpty()) {
                langPreference.setSummary(language);
            }

            langPreference.setOnPreferenceClickListener(preference -> {
                getLanguageRepo.launch(new Intent(getContext(), LanguageRepoActivity.class));
                return false;
            });
        }

        ActivityResultLauncher<Intent> getLanguageRepo =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    setPreferenceScreen(null);
                    setPreferencesFromResource(R.xml.preferences, _rootKey);
                    setup();
                });

    }
}