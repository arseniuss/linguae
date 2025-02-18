package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Configuration;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.SummaryDataAccess;

public class SummaryViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());

    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");

    private final SummaryDataAccess _summaryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetSummaryDataAccess();

    private final MutableLiveData<Bitmap> _image = new MutableLiveData<>(null);
    private final MutableLiveData<String> _version = new MutableLiveData<>("");
    private final MutableLiveData<String> _author = new MutableLiveData<>("");

    private final MutableLiveData<List<BestLessonViewModel>> _lessons =
            new MutableLiveData<>(new ArrayList<>());

    public SummaryViewModel(Application app) {
        super(app);
    }

    public MutableLiveData<Bitmap> Image() {
        return _image;
    }

    public MutableLiveData<String> Version() {
        return _version;
    }

    public MutableLiveData<String> Author() {
        return _author;
    }

    public String LanguageName() {
        return _language;
    }

    public MutableLiveData<List<BestLessonViewModel>> Lessons() {
        return _lessons;
    }

    public void Load() {
        Configuration.AddConfigChangeLister(this::onConfigChanged);

        Disposable d = _summaryDataAccess.GetBestLessons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bestLessons -> {
                    _lessons.setValue(
                            bestLessons.stream().map(l -> new BestLessonViewModel(l)).collect(
                                    Collectors.toList()));
                });
    }

    private void onConfigChanged() {
        Bitmap bitmap = Configuration.GetLanguageImage();

        if (bitmap != null) {
            _image.setValue(Configuration.GetLanguageImage());
        } else {
            Disposable d = Utilities.LoadImage(getApplication().getBaseContext(),
                            Configuration.GetLanguageImageUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(_image::setValue);
        }

        _version.setValue(Configuration.GetLanguageVersion());
        _author.setValue(Configuration.GetLanguageAuthor());
    }

    public static class BestLessonViewModel extends BaseObservable {
        private final SummaryDataAccess.BestLesson _bestLesson;

        public BestLessonViewModel(SummaryDataAccess.BestLesson bestLesson) {
            _bestLesson = bestLesson;
        }

        public String LessonName() {
            return _bestLesson.Name;
        }

        public String Result() {
            return _bestLesson.Points + "/" + _bestLesson.Amount;
        }
    }
}