package lv.id.arseniuss.linguae.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.entities.ItemLanguageRepo;
import lv.id.arseniuss.linguae.entities.Repository;
import lv.id.arseniuss.linguae.parsers.LanguageDataParser;

public class RepositoryLoader implements LanguageDataParser.ParserInterface {
    private final Context _context;
    private final SharedPreferences _sharedPreferences;
    private final LanguageDataParser _dataParser;
    private final String _defaultLanguageRepositories;

    private final List<Pair<String, Repository>> _repositories = new ArrayList<>();

    public RepositoryLoader(Context context) {
        _context = context;
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        _dataParser = new LanguageDataParser(this, Utilities.GetLanguageCodes(_sharedPreferences),
                _sharedPreferences.getBoolean(Constants.PreferenceSaveImagesKey, false));
        _defaultLanguageRepositories = context.getString(R.string.DefaultLanguageRepositories);
    }

    public void Load(OnLoadedListener listener) {
        List<ItemLanguageRepo> repos;
        String jsonRepoList = _sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");

        if (jsonRepoList.isEmpty()) {
            repos = new ArrayList<>();
        } else {
            repos = Utilities.UnpackList(jsonRepoList, ItemLanguageRepo.class);
        }

        if (repos.isEmpty()) {
            for (String repo : _defaultLanguageRepositories.split(",")) {
                Uri uri = Uri.parse(repo);

                repos.add(new ItemLanguageRepo(uri.getHost(), repo));
            }

            saveRepositories(repos);
        }

        loadRepositories(repos, listener);
    }

    private void loadRepositories(List<ItemLanguageRepo> repos, OnLoadedListener listener) {
        List<Single<Pair<String, Repository>>> singles = new ArrayList<>();

        for (ItemLanguageRepo repo : repos) {
            String location = repo.Location;

            Single<Pair<String, Repository>> single = Single.fromCallable(
                    () -> new Pair<>(repo.Name, _dataParser.ParseRepository(location)));

            singles.add(single);
        }

        _repositories.clear();

        Disposable d = Observable.fromIterable(singles)
                .flatMap(Single::toObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addRepository, this::onError, () -> repositoriesLoaded(listener));
        try {
            d.wait();
        } catch (Throwable ignored) {

        }
    }

    private void onError(Throwable throwable) {
        Log.d("DEBUG", Objects.requireNonNull(throwable.getMessage()));
    }

    private void addRepository(Pair<String, Repository> stringLanguageRepositoryPair) {
        _repositories.add(stringLanguageRepositoryPair);
    }

    private void repositoriesLoaded(OnLoadedListener listener) {
        listener.Loaded(_repositories);
    }

    private void saveRepositories(List<ItemLanguageRepo> repos) {
        String jsonRepositories = Utilities.GetGson().toJson(repos);

        _sharedPreferences.edit()
                .putString(Constants.PreferenceRepositoriesKey, jsonRepositories)
                .apply();
    }

    @Override
    public InputStream GetFile(String filename) throws Exception {
        return Utilities.GetInputStream(_context, filename);
    }

    @Override
    public void Inform(int type, String message) {
        Log.i("INFO", message);
    }

    public interface OnLoadedListener {
        void Loaded(List<Pair<String, Repository>> data);
    }
}
