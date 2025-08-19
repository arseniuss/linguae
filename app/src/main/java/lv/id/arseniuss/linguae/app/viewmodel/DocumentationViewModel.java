package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;

import androidx.databinding.BaseObservable;
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
import lv.id.arseniuss.linguae.app.db.dataaccess.LicenseDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.LicenseEntity;

public class DocumentationViewModel extends AndroidViewModel {
    private final LicenseDataAccess _licenseDataAccess =
            LanguageDatabase.GetInstance(getApplication()).GetLicenseDataAccess();

    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>("");
    private final MutableLiveData<List<EntryViewModel>> _licenses =
            new MutableLiveData<>(new ArrayList<>());

    public DocumentationViewModel(Application app) {
        super(app);

        load();
    }

    public MutableLiveData<Boolean> HasError() {
        return _hasError;
    }

    public MutableLiveData<String> GetError() {
        return _error;
    }

    public MutableLiveData<List<EntryViewModel>> GetLicenses() {
        return _licenses;
    }

    private void load() {
        Disposable d = _licenseDataAccess.GetLicenses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::parse, this::handleError);
    }

    private void handleError(Throwable throwable) {
        _hasError.setValue(true);
        _error.setValue(throwable.getMessage());
    }

    private void parse(List<LicenseEntity> result) {
        String copyright = getApplication().getResources().getString(R.string.CopyrightText);
        String wiktionary = getApplication().getResources().getString(R.string.WiktionaryText);

        result.add(0, new LicenseEntity(copyright));
        result.add(1, new LicenseEntity(wiktionary));

        _licenses.postValue(result.stream().map(EntryViewModel::new).collect(Collectors.toList()));
    }

    public static class EntryViewModel extends BaseObservable {
        private final LicenseEntity _licenseEntity;

        public EntryViewModel(LicenseEntity license) {
            _licenseEntity = license;
        }

        public String GetText() {
            return _licenseEntity.Text;
        }
    }
}
