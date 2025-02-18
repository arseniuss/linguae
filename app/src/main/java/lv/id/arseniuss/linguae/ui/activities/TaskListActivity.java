package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityTasklistBinding;
import lv.id.arseniuss.linguae.databinding.ItemTaskBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.TaskListViewModel;

public class TaskListActivity extends AppCompatActivity {
    public static final String LessonExtraTag = "LESSON_NO";
    public static final String LessonNameExtraTag = "LESSON_NAME";

    private final TaskListActivity _this = this;

    private TaskListViewModel _model;
    private ActivityTasklistBinding _binding;

    @BindingAdapter("items")
    public static void BindTaskList(RecyclerView recyclerView,
                                    List<TaskListViewModel.EntryViewModel> tasks) {
        MyRecyclerViewAdapter<TaskListViewModel.EntryViewModel, ItemTaskBinding> adapter =
                (MyRecyclerViewAdapter<TaskListViewModel.EntryViewModel, ItemTaskBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert tasks != null;

        adapter.Update(tasks);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(LessonExtraTag)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_tasklist);
        _model = new ViewModelProvider(this).get(TaskListViewModel.class);

        if (i.hasExtra(LessonExtraTag)) {
            _model.LoadLesson(i.getStringExtra(LessonExtraTag));
        }

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();
        _binding.tasks.setAdapter(adapter);

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

    private RecyclerView.Adapter getMyAdapter() {
        MyRecyclerViewAdapter<TaskListViewModel.EntryViewModel, ItemTaskBinding> adapter =
                new MyRecyclerViewAdapter<>(_binding.getLifecycleOwner(), R.layout.item_task);

        MyRecyclerViewAdapter<TaskListViewModel.EntryViewModel, ItemTaskBinding>.OnBinded binded =
                adapter.new OnBinded() {
                    @Override
                    public void Binded(ItemTaskBinding binding,
                                       TaskListViewModel.EntryViewModel item) {
                        binding.setPresenter(_this);
                    }
                };
        adapter.SetOnBinded(binded);

        return adapter;
    }
}
