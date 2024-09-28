package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.db.entities.Chapter;

public class TheoryViewModel extends AndroidViewModel {
    protected final MutableLiveData<Boolean> _showTranslation = new MutableLiveData<>(true);
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(getApplication().getString(R.string.PreferenceLanguageKey), "");
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTheoryDataAccess();
    private final MutableLiveData<List<ChapterViewModel>> _chapters = new MutableLiveData<>(new ArrayList<>());

    public TheoryViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ChapterViewModel>> Data() { return _chapters; }

    public void LoadTheory(String theoryId) {
        Disposable d = _theoryDataAccess.GetTheoryChapters(theoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chapters -> {
                    _chapters.setValue(chapters.stream()
                            .map(ChapterViewModel::new)
                            .peek(_showTranslation::observeForever)
                            .collect(Collectors.toList()));
                });
    }

    public void LoadLesson(String lessonId) {
        Disposable d = _theoryDataAccess.GetLessonChapters(lessonId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chapters -> {
                    _chapters.setValue(chapters.stream().map(ChapterViewModel::new).collect(Collectors.toList()));
                });
    }

    public void SwitchLanguage() {
        Boolean showTranslationValue = _showTranslation.getValue();

        assert showTranslationValue != null;

        _showTranslation.setValue(!showTranslationValue);
    }

    public static class ChapterViewModel extends BaseObservable implements Observer<Boolean> {
        private final Chapter _chapter;
        private final MutableLiveData<String> _text = new MutableLiveData<>();

        public ChapterViewModel(Chapter c) {
            _chapter = c;
        }

        public MutableLiveData<String> Text() { return _text; }

        @Override
        public void onChanged(Boolean aBoolean) {
            if (aBoolean) {
                _text.setValue(_chapter.Explanation);
            }
            else {
                _text.setValue(_chapter.Translation);
            }
        }
    }
}
