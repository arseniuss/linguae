package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityLessonSummaryBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.LessonSummaryViewModel;

public class LessonSummaryActivity extends AppCompatActivity {
    public static final String LessonExtraTag = "LESSON_NO";
    public static final String LessonNameExtraTag = "LESSON_NAME";

    private final LessonSummaryActivity _this = this;

    private LessonSummaryViewModel _model;
    private ActivityLessonSummaryBinding _binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(LessonExtraTag)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_lesson_summary);
        _model = new ViewModelProvider(this).get(LessonSummaryViewModel.class);

        if (i.hasExtra(LessonExtraTag)) {
            _model.LoadLesson(i.getStringExtra(LessonExtraTag));
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
