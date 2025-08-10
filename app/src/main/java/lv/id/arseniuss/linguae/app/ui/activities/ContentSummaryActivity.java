package lv.id.arseniuss.linguae.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.ActivityContentSummaryBinding;
import lv.id.arseniuss.linguae.app.viewmodel.ContentSummaryViewModel;

public class ContentSummaryActivity extends AppCompatActivity {
    public static final String LessonExtraTag = "LESSON_NO";
    public static final String LessonNameExtraTag = "LESSON_NAME";

    public static final String TrainingExtraTag = "TRAINING_NO";
    public static final String TrainingNameExtraTag = "TRAINING_NAME";

    private final ContentSummaryActivity _this = this;

    private ContentSummaryViewModel _model;
    private ActivityContentSummaryBinding _binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(LessonExtraTag) && !i.hasExtra(TrainingExtraTag)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_content_summary);
        _model = new ViewModelProvider(this).get(ContentSummaryViewModel.class);

        if (i.hasExtra(LessonExtraTag)) {
            _model.LoadLesson(i.getStringExtra(LessonExtraTag));
        } else if (i.hasExtra(TrainingExtraTag)) {
            _model.LoadTraining(i.getStringExtra(TrainingExtraTag));
        }

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);

            if (i.hasExtra(LessonNameExtraTag))
                supportActionBar.setTitle(i.getStringExtra(LessonNameExtraTag));
            else if (i.hasExtra(TrainingNameExtraTag))
                supportActionBar.setTitle(i.getStringExtra(TrainingNameExtraTag));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
