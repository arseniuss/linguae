package lv.id.arseniuss.linguae.app.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.List;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.data.RepositoryLoader;
import lv.id.arseniuss.linguae.app.ui.fragments.LanguageLoadFragment;
import lv.id.arseniuss.linguae.app.ui.fragments.LocaleSelectFragment;
import lv.id.arseniuss.linguae.app.ui.fragments.RepositorySelectFragment;
import lv.id.arseniuss.linguae.entities.Repository;

public class InitialActivity extends AppCompatActivity {
    public static final String ACTION = "Action";

    public static final int CHANGE_DISPLAY_LANGUAGE = 1;
    public static final int CHANGE_REPOSITORY = 2;
    public static final int LOAD_LANGUAGE = 3;

    private RepositoryLoader _loader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.activity_initial);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Fragment(R.layout.fragment_logo))
                    .commit();
        }

        if (intent.hasExtra(ACTION)) {
            switch (intent.getIntExtra(ACTION, 0)) {
                case CHANGE_DISPLAY_LANGUAGE:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,
                                    new LocaleSelectFragment(this::onLocaleSelected))
                            .commit();
                    break;
                case CHANGE_REPOSITORY:
                    _loader = new RepositoryLoader(getBaseContext());

                    _loader.Load(this::onRepositoriesLoaded);
                    break;
                case LOAD_LANGUAGE:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new LanguageLoadFragment(true))
                            .commit();
                    break;
                default:
                    finish();
                    break;
            }

            return;
        }

        String displayLanguage =
                sharedPreferences.getString(Constants.PreferenceLocaleCodeKey, "").trim();

        if (displayLanguage.isEmpty()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new LocaleSelectFragment(this::onLocaleSelected))
                    .commit();
            return;
        }
        else {
            Utilities.SetLocale(this, displayLanguage);
        }

        String language = sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "").trim();
        String languageUrl =
                sharedPreferences.getString(Constants.PreferenceLanguageUrlKey, "").trim();

        if (language.isEmpty() || languageUrl.isEmpty()) {
            _loader = new RepositoryLoader(getBaseContext());

            _loader.Load(this::onRepositoriesLoaded);
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new LanguageLoadFragment())
                .commit();
    }

    private void onRepositoriesLoaded(List<Pair<String, Repository>> data) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container,
                        new RepositorySelectFragment(this, data, this::repositoryLanguageSelected))
                .commit();
    }

    private void repositoryLanguageSelected() {
        Intent intent = getIntent();

        if (intent.hasExtra(ACTION) && intent.getIntExtra(ACTION, 0) == CHANGE_REPOSITORY) {
            Intent i = new Intent(this, InitialActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(ACTION, LOAD_LANGUAGE);

            startActivity(i);
            finish();
        } else {
            restart();
        }
    }

    private void gotoPreferences() {
        Intent i = new Intent(this, PreferencesActivity.class);

        startActivity(i);
        finish();
    }

    private void onLocaleSelected() {
        Intent intent = getIntent();

        if (intent.hasExtra(ACTION)) {
            gotoMain();
        } else {
            restart();
        }
    }

    private void gotoMain() {
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(i);
        finish();
    }

    private void restart() {
        Intent i = new Intent(this, InitialActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(i);
        finish();
    }
}
