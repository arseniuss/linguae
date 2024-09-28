package lv.id.arseniuss.linguae.viewmodel;

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
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TheoryDataAccess;

public class TheoriesViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), "");
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTheoryDataAccess();
    private final MutableLiveData<List<EntryViewModel>> _theories =
            new MutableLiveData<List<EntryViewModel>>(new ArrayList<>());

    public TheoriesViewModel(@NonNull Application application) {
        super(application);

        loadData();
    }

    public LiveData<List<EntryViewModel>> Data() { return _theories; }

    void loadData() {
        Disposable d = _theoryDataAccess.GetTheories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(theoryWithCounts -> {
                    _theories.setValue(theoryWithCounts.stream()
                            .sorted(Comparator.comparingInt(t -> t.Theory.Index))
                            .map(EntryViewModel::new)
                            .collect(Collectors.toList()));
                });
    }

    public TheoryDataAccess.TheoryWithCount GetTheory(int selection) {
        TheoryDataAccess.TheoryWithCount res = null;

        try {
            EntryViewModel viewModel = _theories.getValue().get(selection);

            res = viewModel._theory;
        } catch (NullPointerException ignore) {

        }

        return res;
    }

    public static class EntryViewModel extends BaseObservable {
        private final TheoryDataAccess.TheoryWithCount _theory;

        public EntryViewModel(TheoryDataAccess.TheoryWithCount t) {
            _theory = t;
        }

        @Bindable("Title")
        public String getTitle() { return _theory.Theory.Title; }

        public String getChapterCountStr() { return String.valueOf(_theory.ChapterCount); }

        @Bindable("Description")
        public String getDescription() { return _theory.Theory.Description; }

        public String getId() {
            return _theory.Theory.Id;
        }

        public int getChapterCount() { return _theory.ChapterCount; }
    }
}
