package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivitySessionBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.ui.CasingFragment;
import lv.id.arseniuss.linguae.tasks.ui.ChooseFragment;
import lv.id.arseniuss.linguae.tasks.ui.ConjugateFragment;
import lv.id.arseniuss.linguae.tasks.ui.DeclineFragment;
import lv.id.arseniuss.linguae.tasks.ui.MacronFragment;
import lv.id.arseniuss.linguae.tasks.ui.NumberFragment;
import lv.id.arseniuss.linguae.tasks.ui.TranslateFragment;
import lv.id.arseniuss.linguae.viewmodel.SessionViewModel;

public class SessionActivity extends AppCompatActivity {
    public static final String LessonExtraTag = "LESSON";
    public static final String TrainingExtraTag = "TRAINING";

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

        if (i.hasExtra(LessonExtraTag)) { _model.LoadLesson(i.getStringExtra(LessonExtraTag), this::loaded); }
        else if (i.hasExtra(TrainingExtraTag)) {
            _model.LoadTraining(i.getStringExtra(TrainingExtraTag), this::loaded);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new MaterialAlertDialogBuilder(_this).setMessage(R.string.MessageEditSession)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            finish();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> { })
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
                }
                else {
                    _model.NextTask(this::loaded);
                }
            }
            else {
                _binding.btnContinue.setText(R.string.ButtonContinueText);
            }
        }
        else {
            if (_model.CurrentTaskIndex == _model.GetTaskCount() - 1) {
                Done();
            }
            else {
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
        }
        else {
            createFragment();
        }
    }

    private void createFragment() {
        SessionTaskData data = _model.GetTask(_model.CurrentTaskIndex);

        switch (data.Task.Type) {
            case CasingTask:
                _currentFragment = new CasingFragment(data);
                break;
            case ChooseTask:
                _currentFragment = new ChooseFragment(data);
                break;
            case ConjugateTask:
                _currentFragment = new ConjugateFragment(data);
                break;
            case DeclineTask:
                _currentFragment = new DeclineFragment(data);
                break;
            case MacronTask:
                _currentFragment = new MacronFragment(data);
                break;
            case NumberTask:
                _currentFragment = new NumberFragment(data);
                break;
            case TranslateTask:
                _currentFragment = new TranslateFragment(data);
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, _currentFragment).commit();
    }
}
