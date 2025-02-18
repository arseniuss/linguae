package lv.id.arseniuss.linguae.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.SessionDataAccess;
import lv.id.arseniuss.linguae.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.db.entities.TaskError;
import lv.id.arseniuss.linguae.db.entities.TaskResult;

public class SessionResultViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");

    private final SessionDataAccess _sessionDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetSessionDataAccess();
    private SessionResultWithTaskResults _result;

    private List<TaskErrorViewModel> _errors = new ArrayList<>();

    public SessionResultViewModel(@NonNull Application application) {
        super(application);
    }


    public List<TaskErrorViewModel> Errors() {
        return _errors;
    }

    public int GetPoints() {
        return _result.SessionResult.Points;
    }

    public int GetAmount() {
        return _result.SessionResult.Amount;
    }

    public String GetPointsTitle() {
        return _result.SessionResult.Points + "/" + _result.SessionResult.Amount;
    }

    public String GetPassedTime() {
        return _result.SessionResult.PassedTime + " s";
    }

    public void SetResult(SessionResultWithTaskResults result, Action onLoaded) {
        _result = result;
        save(onLoaded);

        List<TaskError> taskErrors = _result.TaskResults.stream().flatMap(r -> r.Errors.stream()).collect(Collectors.toList());

        _errors = taskErrors.stream().map(TaskErrorViewModel::new).collect(Collectors.toList());
    }

    @SuppressLint("CheckResult")
    private void save(Action onLoaded) {
        _sessionDataAccess.SaveResult(_result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onLoaded::run);
    }

    public List<TaskResult> GetTaskResults() {
        return _result.TaskResults;
    }

    public static class TaskErrorViewModel extends BaseObservable {
        private final TaskError _taskError;

        public TaskErrorViewModel(TaskError taskError) {
            _taskError = taskError;
        }

        public String TaskName() {
            return _taskError.Type.GetName();
        }

        public Spanned GetResult() {
            return Html.fromHtml("<strike>" + _taskError.IncorrectAnswer + "</strike>  " +
                    _taskError.CorrectAnswer, Html.FROM_HTML_MODE_LEGACY);
        }
    }
}
