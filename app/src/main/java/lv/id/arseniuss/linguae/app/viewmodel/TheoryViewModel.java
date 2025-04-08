package lv.id.arseniuss.linguae.app.viewmodel;

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
import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.ChapterEntity;

public class TheoryViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageCodeKey, "");
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTheoryDataAccess();
    private final MutableLiveData<List<ChapterViewModel>> _chapters =
            new MutableLiveData<>(new ArrayList<>());
    protected boolean _showTranslation = false;

    public TheoryViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ChapterViewModel>> Data() {
        return _chapters;
    }

    public void LoadTheory(String theoryId) {
        Disposable d = _theoryDataAccess.GetTheoryChapters(theoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setChapters);
    }

    public void LoadLesson(String lessonId) {
        Disposable d = _theoryDataAccess.GetLessonChapters(lessonId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setChapters);
    }

    public void SwitchLanguage() {
        _showTranslation = !_showTranslation;

        List<ChapterViewModel> chapterViewModels = _chapters.getValue();

        assert chapterViewModels != null;

        for (ChapterViewModel chapterViewModel : chapterViewModels) {
            chapterViewModel.onChanged(_showTranslation);
        }
    }

    private void setChapters(List<ChapterEntity> chapters) {
        _chapters.setValue(chapters.stream()
                .map(ChapterViewModel::new)
                .collect(Collectors.toList()));
    }

    public static class ChapterViewModel extends BaseObservable implements Observer<Boolean> {
        private final ChapterEntity _chapterEntity;
        private final MutableLiveData<String> _text = new MutableLiveData<>();

        public ChapterViewModel(ChapterEntity c) {
            _chapterEntity = c;
            _text.setValue(c.Explanation);
        }

        public MutableLiveData<String> Text() {
            return _text;
        }

        @Override
        public void onChanged(Boolean showTranslation) {
            if (showTranslation) {
                _text.setValue(_chapterEntity.Translation);
            } else {
                _text.setValue(_chapterEntity.Explanation);
            }
        }
    }
}
