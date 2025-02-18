package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
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
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.db.entities.Task;

public class TaskListViewModel extends AndroidViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language =
            _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TaskDataAccess _taskDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTaskDataAccess();

    private final MutableLiveData<List<EntryViewModel>> _tasks =
            new MutableLiveData<>(new ArrayList<>());

    public TaskListViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<EntryViewModel>> Data() {
        return _tasks;
    }

    public void LoadLesson(String lessonNo) {
        Disposable d = _taskDataAccess.GetTasksByLesson(lessonNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setTasks);
    }

    private void setTasks(List<Task> tasks) {
        _tasks.setValue(tasks.stream()
                .map(EntryViewModel::new)
                .collect(Collectors.toList()));
    }

    public static class EntryViewModel extends BaseObservable {
        private final Task _task;

        public EntryViewModel(Task task) {
            _task = task;
        }

        public String Type() {
            return _task.Type.GetName();
        }

        public String Title() {
            return _task.Data.GetTitle();
        }
    }
}
