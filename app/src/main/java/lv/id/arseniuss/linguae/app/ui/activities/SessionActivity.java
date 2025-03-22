package lv.id.arseniuss.linguae.app.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.databinding.ActivitySessionBinding;
import lv.id.arseniuss.linguae.app.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.app.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.app.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.app.tasks.ui.ChooseFragment;
import lv.id.arseniuss.linguae.app.tasks.ui.ConjugateFragment;
import lv.id.arseniuss.linguae.app.tasks.ui.DeclineFragment;
import lv.id.arseniuss.linguae.app.tasks.ui.SelectFragment;
import lv.id.arseniuss.linguae.app.tasks.ui.TranslateFragment;
import lv.id.arseniuss.linguae.app.ui.dialogs.BugReportDialogFragment;
import lv.id.arseniuss.linguae.app.viewmodel.SessionViewModel;

public class SessionActivity extends AppCompatActivity
        implements AbstractTaskFragment.TaskChangeListener,
        BugReportDialogFragment.DialogActionListener {
    public static final String LessonExtraTag = "LESSON";
    public static final String TrainingExtraTag = "TRAINING";
    public static final String TrainingCategoriesExtraTag = "TRAINING_CATEGORIES";

    private final SessionActivity _this = this;

    protected SessionViewModel _model;
    private AbstractTaskFragment _currentFragment;
    private ActivitySessionBinding _binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(LessonExtraTag) && !i.hasExtra(TrainingExtraTag)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_session);
        _model = new ViewModelProvider(this).get(SessionViewModel.class);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        if (i.hasExtra(LessonExtraTag)) {
            _model.LoadLesson(i.getStringExtra(LessonExtraTag), this::loaded);
        } else if (i.hasExtra(TrainingExtraTag)) {
            Type listType = new TypeToken<List<TaskDataAccess.TrainingCategory>>() {
            }.getType();
            List<TaskDataAccess.TrainingCategory> categories = new ArrayList<>();

            if (i.hasExtra(TrainingCategoriesExtraTag)) {
                categories =
                        new Gson().fromJson(i.getStringExtra(TrainingCategoriesExtraTag), listType);
            }

            _model.LoadTraining(i.getStringExtra(TrainingExtraTag), categories, this::loaded);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new MaterialAlertDialogBuilder(_this).setMessage(R.string.MessageEditSession)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        })
                        .show();
            }
        });
    }

    public void Done() {
        _model.Done();

        Intent i = new Intent(this, SessionResultActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(SessionResultActivity.ResultTag, _model.GetResult());

        startActivity(i);
        finish();
    }

    public void OnReportBug() {
        BugReportDialogFragment bugReportDialogFragment = new BugReportDialogFragment();

        _model.SetCounterRunning(false);

        bugReportDialogFragment.show(getSupportFragmentManager(), "fragment-report-bug");
    }

    public void OnCheckClicked() {
        if (!_currentFragment.IsValidated()) {
            if (_currentFragment.Validate()) {
                if (_model.CurrentTaskIndex == _model.GetTaskCount() - 1) {
                    Done();
                } else {
                    _model.NextTask(this::loaded);
                }
            } else {
                _binding.btnContinue.setText(R.string.ButtonContinueText);
            }
        } else {
            if (_model.CurrentTaskIndex == _model.GetTaskCount() - 1) {
                Done();
            } else {
                _binding.btnContinue.setText(R.string.ButtonCheckTitle);
                _model.NextTask(this::loaded);
            }
        }
    }

    private void loaded(String error) {
        if (error != null) {
            new MaterialAlertDialogBuilder(this).setMessage(error)
                    .setNeutralButton(R.string.ok, (dialog, which) -> finish())
                    .show();
        } else {
            createFragment();
        }
    }

    private void createFragment() {
        SessionTaskData data = _model.GetTask(_model.CurrentTaskIndex);

        switch (data.Task.Type) {
            case SelectTask:
                _currentFragment = new SelectFragment(data, this);
                break;
            case ChooseTask:
                _currentFragment = new ChooseFragment(data, this);
                break;
            case ConjugateTask:
                _currentFragment = new ConjugateFragment(data, this);
                break;
            case DeclineTask:
                _currentFragment = new DeclineFragment(data, this);
                break;
            case TranslateTask:
                _currentFragment = new TranslateFragment(data, this);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, _currentFragment)
                .commit();
    }

    @Override
    public void OnCanCheckChanged(boolean canCheck) {
        _model.CanCheck().setValue(canCheck);
    }

    @Override
    public void OnCancelBugReport() {
        _model.SetCounterRunning(true);
    }

    @Override
    public void OnApplicationBug() {
        Intent i = new Intent(this, BugReportActivity.class);
        View rootView = getWindow().getDecorView().getRootView();
        SessionTaskData sessionTaskData = _model.GetTask(_model.CurrentTaskIndex);
        String taskData = new Gson().toJson(sessionTaskData.Task);

        rootView.setDrawingCacheEnabled(true);

        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());

        rootView.setDrawingCacheEnabled(false);

        i.putExtra(BugReportActivity.TASK_KEY, taskData);
        i.putExtra(BugReportActivity.IMAGE_KEY, Utilities.BitmapToBase64(bitmap));

        startActivity(i);
    }

    @Override
    public void OnContentBug() {
        Intent i = new Intent(this, BugReportActivity.class);
        SessionTaskData sessionTaskData = _model.GetTask(_model.CurrentTaskIndex);
        String taskData = new Gson().toJson(sessionTaskData.Task);

        i.putExtra(BugReportActivity.TASK_KEY, taskData);

        startActivity(i);
    }
}
