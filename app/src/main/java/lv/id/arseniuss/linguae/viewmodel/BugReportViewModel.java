package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.BugReportDataAccess;
import lv.id.arseniuss.linguae.db.entities.Task;

public class BugReportViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final BugReportDataAccess _bugReportDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetBugReportDataAccess();
    private Task _task;

    public BugReportViewModel(@NonNull Application application) {
        super(application);
    }

    public void Save() {

    }

    public void Load(String taskJson) {
        _task = Task.GetGson().fromJson(taskJson, Task.class);
    }
}
