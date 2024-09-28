package lv.id.arseniuss.linguae.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.SessionDataAccess;
import lv.id.arseniuss.linguae.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.db.entities.TaskResult;

public class SessionResultViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(
            getApplication().getString(R.string.PreferenceLanguageKey), "");

    private final SessionDataAccess _sessionDataAccess = LanguageDatabase.GetInstance(getApplication(), _language)
            .GetSessionDataAccess();
    private SessionResultWithTaskResults _result;

    public SessionResultViewModel(@NonNull Application application) {
        super(application);
    }

    public int GetPoints() { return _result.SessionResult.Points; }

    public int GetAmount() { return _result.SessionResult.Amount; }

    public String GetPassedTime() { return String.valueOf(_result.SessionResult.PassedTime); }

    public void SetResult(SessionResultWithTaskResults result, Action onLoaded) {
        _result = result;
        save(onLoaded);
    }

    @SuppressLint("CheckResult")
    private void save(Action onLoaded) {
        _sessionDataAccess.SaveResult(_result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onLoaded::run);
    }

    public List<TaskResult> GetTaskResults() { return _result.TaskResults; }
}
