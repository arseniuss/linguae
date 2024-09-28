package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.ItemLanguageRepo;

public class LanguageRepoViewModel extends AndroidViewModel {

    private final String _languageKey = getApplication().getString(R.string.PreferenceLanguageKey);
    private final String _languageListKey = getApplication().getString(R.string.PreferenceLanguageListKey);
    private final String _languageLocationKey = getApplication().getString(R.string.PreferenceLanguageLocationKey);
    private final SharedPreferences _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            getApplication().getBaseContext());
    private final MutableLiveData<List<LanguageRepoItemViewModel>> _repos;
    private final MutableLiveData<Integer> _selected;

    public LanguageRepoViewModel(@NonNull Application app) {
        super(app);
        _repos = new MutableLiveData<>(new ArrayList<>());
        _selected = new MutableLiveData<>(-1);
        loadData();
    }

    public LiveData<List<LanguageRepoItemViewModel>> Data() {
        return _repos;
    }

    public LiveData<Integer> Selected() { return _selected; }

    private void loadData() {
        String languagesJson = _sharedPreferences.getString(_languageListKey, "");
        Type listOfMyClassObject = new TypeToken<ArrayList<ItemLanguageRepo>>() { }.getType();
        List<ItemLanguageRepo> repos = new Gson().fromJson(languagesJson, listOfMyClassObject);

        String language = _sharedPreferences.getString(_languageKey, "");

        if (repos == null) repos = new ArrayList<>();

        _repos.setValue(repos.stream().map(LanguageRepoItemViewModel::new).collect(Collectors.toList()));

        _selected.setValue(repos.stream().map(r -> r.Name).collect(Collectors.toList()).indexOf(language));
    }

    public void SaveData() {
        Type listOfMyClassObject = new TypeToken<ArrayList<ItemLanguageRepo>>() { }.getType();
        List<ItemLanguageRepo> repos = Objects.requireNonNull(_repos.getValue())
                .stream()
                .map(LanguageRepoItemViewModel::getModel)
                .collect(Collectors.toList());

        String string = new Gson().toJson(repos, listOfMyClassObject);

        _sharedPreferences.edit().putString(_languageListKey, string).apply();

        Selected(_selected.getValue());
    }

    public void Selected(int selection) {
        LanguageRepoItemViewModel itemViewModel = Objects.requireNonNull(_repos.getValue()).get(selection);

        String language = itemViewModel.getName();
        String location = itemViewModel.getLocation();

        _sharedPreferences.edit().putString(_languageKey, language).putString(_languageLocationKey, location).apply();
    }

    public void Add(LanguageRepoItemViewModel model) {
        List<LanguageRepoItemViewModel> repos = _repos.getValue();

        if (repos == null) repos = new ArrayList<>();
        repos.add(model);

        _repos.setValue(repos);
    }
}
