package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.CommonDataAccess;

public class VocabularyViewModel extends AndroidViewModel {

    private final CommonDataAccess _commonDataAccess =
            LanguageDatabase.GetInstance(getApplication()).GetCommonDataAccess();

    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(true);
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    private final MutableLiveData<List<ItemMarkdownViewModel>> _vocabulary =
            new MutableLiveData<>(new ArrayList<>());

    public VocabularyViewModel(@NonNull Application application) {
        super(application);
    }

    private void accept(List<String> strings, Throwable throwable) {
        if (throwable != null) {
            _error.setValue(""); // TODO
            _hasError.setValue(true);
        } else if (strings.isEmpty()) {
            _error.setValue(getApplication().getResources().getString(R.string.EmptyListMessage));
            _hasError.setValue(true);
        } else {
            _vocabulary.setValue(strings.stream()
                    .map(s -> String.format("[%1$s](wikt:%1$s)", s))
                    .map(ItemMarkdownViewModel::new)
                    .collect(Collectors.toList()));
            _hasError.setValue(false);
        }
    }

    public MutableLiveData<Boolean> HasError() {
        return _hasError;
    }

    public MutableLiveData<String> Error() {
        return _error;
    }

    public MutableLiveData<List<ItemMarkdownViewModel>> Vocabulary() {
        return _vocabulary;
    }

    public void Load() {
        Disposable d = _commonDataAccess.GetVocabulary()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::accept);
    }
}
