package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class SessionViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
    private final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");
    private final TaskDataAccess _taskDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetTaskDataAccess();
    private final MutableLiveData<String> _counterString = new MutableLiveData<>("0");
    private final MutableLiveData<String> _taskProgress = new MutableLiveData<>("");
    private final SessionResultWithTaskResults _result = new SessionResultWithTaskResults();

    public int CurrentTaskIndex = -1;
    ScheduledExecutorService _scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private int _counter = 0;
    private List<SessionTaskData> _tasks = new ArrayList<>();

    public SessionViewModel(@NonNull Application application) {
        super(application);

        _scheduledExecutorService.scheduleAtFixedRate(() -> {
            _counter += 1;
            _counterString.postValue(_counter + " s");
        }, 0, 1, TimeUnit.SECONDS);
    }

    public MutableLiveData<String> Counter() { return _counterString; }

    public MutableLiveData<String> TaskProgress() { return _taskProgress; }

    public void LoadLesson(String lessonId, ILoaded loaded) {

        int taskCount = _sharedPreferences.getInt(Constants.PreferenceTaskCountKey, 10);

        Disposable d = _taskDataAccess.SelectLessonTasks(lessonId, taskCount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((tasks) -> {
                    _tasks = tasks.stream().map(SessionTaskData::new).collect(Collectors.toList());
                    NextTask(loaded);
                }, throwable -> loaded.Loaded(throwable.getMessage()));
    }

    public void NextTask(ILoaded loaded) {
        if (CurrentTaskIndex < _tasks.size()) {
            CurrentTaskIndex += 1;

            UpdateTaskProgress();
        }

        loaded.Loaded(null);
    }

    private void UpdateTaskProgress() {
        _taskProgress.setValue((CurrentTaskIndex + 1) + "/" + _tasks.size());
    }

    public void LoadTraining(String training, ILoaded loaded) {
        int taskCount = _sharedPreferences.getInt(Constants.PreferenceTaskCountKey, 10);

        Disposable d = _taskDataAccess.SelectTrainingTasks(training, taskCount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((tasks) -> {
                    _tasks = tasks.stream().map(SessionTaskData::new).collect(Collectors.toList());
                    NextTask(loaded);
                }, throwable -> loaded.Loaded(throwable.getMessage()));
    }

    public SessionTaskData GetTask(int currentIndex) {
        return _tasks.get(currentIndex);
    }

    public int GetTaskCount() { return _tasks.size(); }

    public void Done() {
        _scheduledExecutorService.shutdown();
    }

    public String GetResult() {
        _result.SessionResult.StartTime = new Date();
        _result.SessionResult.PassedTime = _counter;
        _result.SessionResult.Points = _tasks.stream().map(t -> t.Result.Points).reduce(0, Integer::sum);
        _result.SessionResult.Amount = _tasks.stream().map(t -> t.Result.Amount).reduce(0, Integer::sum);
        _result.TaskResults = _tasks.stream().map(t -> t.Result).collect(Collectors.toList());

        return new Gson().toJson(_result);
    }

    public interface ILoaded {
        void Loaded(String errorMessage);
    }
}
