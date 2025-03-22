package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.BugReportDataAccess;
import lv.id.arseniuss.linguae.app.db.entities.TaskEntity;

public class BugReportViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final BugReportDataAccess _bugReportDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetBugReportDataAccess();
    private TaskEntity _task;

    public BugReportViewModel(@NonNull Application application) {
        super(application);
    }

    public void Save() {

    }

    public void Load(String taskJson) {
        _task = TaskEntity.GetGson().fromJson(taskJson, TaskEntity.class);
    }
}
