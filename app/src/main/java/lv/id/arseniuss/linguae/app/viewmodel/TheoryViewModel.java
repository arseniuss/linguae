package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.ChapterEntity;

public class TheoryViewModel extends AndroidViewModel {
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication()).GetTheoryDataAccess();
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

    private void setChapters(List<ChapterEntity> chapters) {
        _chapters.setValue(chapters.stream()
                .map(ChapterViewModel::new)
                .collect(Collectors.toList()));
    }

    public static class ChapterViewModel extends BaseObservable {
        private final ChapterEntity _chapterEntity;
        private final MutableLiveData<String> _text = new MutableLiveData<>();

        public ChapterViewModel(ChapterEntity c) {
            _chapterEntity = c;
            _text.setValue(c.Text);
        }

        public MutableLiveData<String> Text() {
            return _text;
        }
    }
}
