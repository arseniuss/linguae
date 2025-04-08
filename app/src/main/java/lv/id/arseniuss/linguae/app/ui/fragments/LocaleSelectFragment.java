package lv.id.arseniuss.linguae.app.ui.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.FragmentLocaleSelectBinding;

public class LocaleSelectFragment extends Fragment {
    private final LanguageSelectedListener _listener;
    private FragmentLocaleSelectBinding _binding;

    public LocaleSelectFragment(LanguageSelectedListener listener) {
        _listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _binding = FragmentLocaleSelectBinding.inflate(inflater, container, false);

        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        return _binding.getRoot();
    }

    public void OnEnglishSelected() {
        setLocale("en", getString(R.string.EnglishLocaleName));
    }

    public void OnLatvianSelected() {
        setLocale("lv", getString(R.string.LatvianLocaleName));
    }

    private void setLocale(String languageCode, String languageName) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity().getBaseContext());

        sharedPreferences.edit()
                .putString(Constants.PreferenceLocaleCodeKey, languageCode)
                .putString(Constants.PreferenceLocaleNameKey, languageName)
                .apply();

        Locale locale = new Locale(languageCode);
        Configuration configuration = new Configuration();

        Locale.setDefault(locale);
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        _listener.LanguageSelected();
    }

    public interface LanguageSelectedListener {
        void LanguageSelected();
    }
}
