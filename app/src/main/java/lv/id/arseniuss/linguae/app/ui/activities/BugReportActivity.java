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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.ActivityBugReportBinding;
import lv.id.arseniuss.linguae.app.viewmodel.BugReportViewModel;

public class BugReportActivity extends AppCompatActivity {
    public final static String IMAGE_KEY = "IMAGE";
    public final static String TASK_KEY = "TASK";

    protected BugReportViewModel _model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        if (!i.hasExtra(TASK_KEY))
            finish();

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        ActivityBugReportBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_bug_report);
        _model = new ViewModelProvider(this).get(BugReportViewModel.class);

        binding.setViewmodel(_model);
        binding.setPresenter(this);
        binding.setLifecycleOwner(this);

        String taskJson = i.getStringExtra(TASK_KEY);

        _model.Load(taskJson);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.SaveBugReportTitle))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        _model.Save();
                        finish();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        finish();
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
