package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.entities.Language;
import lv.id.arseniuss.linguae.entities.Repository;

public class RepositorySelectViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final MutableLiveData<List<RepositoryViewModel>> _repositories =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> _selectedRepository = new MutableLiveData<>(0);
    private final MutableLiveData<RepositoryViewModel> _repository = new MutableLiveData<>(null);

    public RepositorySelectViewModel(@NonNull Application application) {
        super(application);
    }

    public void LoadRepository(Integer selectedRepository) {
        List<RepositoryViewModel> repositories = _repositories.getValue();

        if (repositories == null) return;

        RepositoryViewModel repository = repositories.get(selectedRepository);

        _repository.postValue(repository);
    }

    public MutableLiveData<List<RepositoryViewModel>> Repositories() {
        return _repositories;
    }

    public MutableLiveData<Integer> SelectedRepository() {
        return _selectedRepository;
    }

    public MutableLiveData<RepositoryViewModel> Repository() {
        return _repository;
    }

    public void SetData(List<Pair<String, Repository>> data) {
        String languageUrl =
                _sharedPreferences.getString(Constants.PreferenceLanguageUrlKey, "").trim();

        // remember selected repository
        if (!languageUrl.isEmpty())
            for (int i = 0; i < data.size(); i++) {
                String repositoryUrl = data.get(i).second.Location;

                if (languageUrl.startsWith(repositoryUrl)) {
                    Pair<String, Repository> removed = data.remove(i);
                    data.add(0, removed);

                    break;
                }
            }

        _repositories.setValue(
                data.stream()
                        .map(d -> new RepositoryViewModel(d.first, d.second))
                        .collect(Collectors.toList()));
    }

    public void LanguageSelected(int position) {
        List<RepositoryViewModel> repositories = _repositories.getValue();

        assert repositories != null;
        RepositoryViewModel repository = repositories.get(_selectedRepository.getValue());

        List<LanguageViewModel> languages = repository.Languages().getValue();

        assert languages != null;
        LanguageViewModel language = languages.get(position);

        _sharedPreferences.edit()
                .putString(Constants.PreferenceLanguageKey, language.Name)
                .putString(Constants.PreferenceLanguageUrlKey, language.LanguageUrl)
                .putString(Constants.PreferenceRepositoryKey, repository.Title)
                .apply();
    }

    public static class RepositoryViewModel extends BaseObservable {
        public final String Name;
        public final String Title;
        public final String Error;
        private final MutableLiveData<Integer> _state =
                new MutableLiveData<>(State.STATE_ERROR.ordinal());
        private final MutableLiveData<List<LanguageViewModel>> _languages =
                new MutableLiveData<>(null);

        public RepositoryViewModel(String name, Repository value) {
            Name = name;
            Title = value.Name;
            Error = value.Error;
            _languages.setValue(value.Languages.stream().map(LanguageViewModel::new).collect(
                    Collectors.toList()));
            if (value.Error.isEmpty()) _state.setValue(State.STATE_DATA.ordinal());
        }

        public MutableLiveData<Integer> State() {
            return _state;
        }

        public MutableLiveData<List<LanguageViewModel>> Languages() {
            return _languages;
        }

        public enum State {
            STATE_DATA,
            STATE_ERROR
        }
    }

    public static class LanguageViewModel extends BaseObservable {
        public final String Name;
        public final String ImageUrl;
        public final String LanguageUrl;
        private final MutableLiveData<String> _language = new MutableLiveData<>();

        public LanguageViewModel(Language l) {
            Name = l.Name;
            _language.setValue(l.Name);
            ImageUrl = l.Image;
            LanguageUrl = l.Location;
        }

        public MutableLiveData<String> Language() {
            return _language;
        }
    }
}
