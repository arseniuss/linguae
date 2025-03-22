package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;

public class TheoriesViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTheoryDataAccess();

    private final MutableLiveData<List<EntryViewModel>> _theories =
            new MutableLiveData<List<EntryViewModel>>(new ArrayList<>());
    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>("");

    public TheoriesViewModel(@NonNull Application application) {
        super(application);

        loadData();
    }

    public LiveData<List<EntryViewModel>> Data() {
        return _theories;
    }

    public MutableLiveData<Boolean> HasError() {
        return _hasError;
    }

    public MutableLiveData<String> GetError() {
        return _error;
    }

    void loadData() {
        Disposable d = _theoryDataAccess.GetTheories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(theoryWithCounts -> {
                    _theories.setValue(theoryWithCounts.stream()
                            .sorted(Comparator.comparingInt(t -> t.Theory.Index))
                            .map(EntryViewModel::new)
                            .collect(Collectors.toList()));
                }, this::handleError);
    }

    private void handleError(Throwable throwable) {
        _hasError.setValue(true);
        _error.setValue(throwable.getMessage());
    }

    public TheoryDataAccess.TheoryWithCount GetTheory(int selection) {
        TheoryDataAccess.TheoryWithCount res = null;
        EntryViewModel viewModel = Objects.requireNonNull(_theories.getValue()).get(selection);

        res = viewModel._theory;

        return res;
    }

    public static class EntryViewModel extends BaseObservable {
        private final TheoryDataAccess.TheoryWithCount _theory;

        public EntryViewModel(TheoryDataAccess.TheoryWithCount t) {
            _theory = t;
        }

        @Bindable("Title")
        public String getTitle() {
            return _theory.Theory.Title;
        }

        public String getChapterCountStr() {
            return String.valueOf(_theory.ChapterCount);
        }

        @Bindable("Description")
        public String getDescription() {
            return _theory.Theory.Description;
        }

        public String getId() {
            return _theory.Theory.Id;
        }

        public int getChapterCount() {
            return _theory.ChapterCount;
        }
    }
}
