package lv.id.arseniuss.linguae;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.db.entities.TaskResult;
import lv.id.arseniuss.linguae.ui.activities.SessionResultActivity;

@RunWith(AndroidJUnit4.class)
public class SessionResultActivityTest {
    @Test
    public void StartSessionResultActivity() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit().putString(Constants.PreferenceLanguageKey, "Latin").apply();

        Intent i = new Intent(context, SessionResultActivity.class);

        i.putExtra(SessionResultActivity.ResultTag, GetResults());

        ActivityScenario<Activity> scenario = ActivityScenario.launch(i);

        scenario.onActivity(activity -> {

        });

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.schedule(() -> scenario.moveToState(Lifecycle.State.DESTROYED), 10, TimeUnit.SECONDS);

        while (scenario.getState() != Lifecycle.State.DESTROYED) ;
    }

    private String GetResults() {
        SessionResultWithTaskResults results = new SessionResultWithTaskResults();

        results.SessionResult.Id = 1;
        results.SessionResult.StartTime = new Date();
        results.SessionResult.PassedTime = 5;

        results.TaskResults.add(new TaskResult(TaskType.CasingTask, 1, 1));
        results.TaskResults.add(new TaskResult(TaskType.CasingTask, 1, 1));
        results.TaskResults.add(new TaskResult(TaskType.CasingTask, 1, 2));
        results.TaskResults.add(new TaskResult(TaskType.DeclineTask, 1, 4));
        results.TaskResults.add(new TaskResult(TaskType.DeclineTask, 3, 4));
        results.TaskResults.add(new TaskResult(TaskType.DeclineTask, 4, 4));
        results.TaskResults.add(new TaskResult(TaskType.CasingTask, 1, 1));
        results.TaskResults.add(new TaskResult(TaskType.ConjugateTask, 1, 1));

        results.SessionResult.Points = results.TaskResults.stream().map(r -> r.Points).reduce(0, Integer::sum);
        results.SessionResult.Amount = results.TaskResults.stream().map(r -> r.Amount).reduce(0, Integer::sum);

        return new Gson().toJson(results);

    }
}
