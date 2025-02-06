package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;

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

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivitySessionBinding;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.ui.SelectFragment;
import lv.id.arseniuss.linguae.tasks.ui.ChooseFragment;
import lv.id.arseniuss.linguae.tasks.ui.ConjugateFragment;
import lv.id.arseniuss.linguae.tasks.ui.DeclineFragment;
import lv.id.arseniuss.linguae.tasks.ui.MacronFragment;
import lv.id.arseniuss.linguae.tasks.ui.NumberFragment;
import lv.id.arseniuss.linguae.tasks.ui.TranslateFragment;
import lv.id.arseniuss.linguae.viewmodel.SessionViewModel;

public class SessionActivity extends AppCompatActivity
        implements AbstractTaskFragment.TaskChangeListener {
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
                categories = new Gson().fromJson(i.getStringExtra(TrainingCategoriesExtraTag), listType);
            }

            _model.LoadTraining(i.getStringExtra(TrainingExtraTag), categories, this::loaded);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new MaterialAlertDialogBuilder(_this).setMessage(R.string.MessageEditSession)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            finish();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
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

    public void OnReportABug() {

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
                    .setNeutralButton(android.R.string.ok, (dialog, which) -> finish())
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
            case MacronTask:
                _currentFragment = new MacronFragment(data, this);
                break;
            case NumberTask:
                _currentFragment = new NumberFragment(data, this);
                break;
            case TranslateTask:
                _currentFragment = new TranslateFragment(data, this);
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, _currentFragment).commit();
    }

    @Override
    public void OnCanCheckChanged(boolean canCheck) {
        _model.CanCheck().setValue(canCheck);
    }
}
