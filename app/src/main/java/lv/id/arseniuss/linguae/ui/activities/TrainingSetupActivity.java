package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityTrainingSetupBinding;
import lv.id.arseniuss.linguae.databinding.ItemTrainingCategoryBinding;
import lv.id.arseniuss.linguae.databinding.ItemTrainingTaskBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.TrainingSetupViewModel;

public class TrainingSetupActivity extends AppCompatActivity {
    public static final String TRAINING = "TRAINING_ID";
    private final TrainingSetupActivity _this = this;
    private TrainingSetupViewModel _model;
    private ActivityTrainingSetupBinding _binding;

    @BindingAdapter("items")
    public static void BindTrainingList(RecyclerView recyclerView,
                                        List<TrainingSetupViewModel.TrainingTaskViewModel> trainings) {
        MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingTaskViewModel, ItemTrainingTaskBinding> adapter =
                (MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingTaskViewModel, ItemTrainingTaskBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert trainings != null;

        adapter.Update(trainings);
    }

    @BindingAdapter("items")
    public static void BindCategoryList(RecyclerView recyclerView,
                                        List<TrainingSetupViewModel.TrainingCategoryViewModel> categories) {
        MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingCategoryViewModel, ItemTrainingCategoryBinding> adapter =
                (MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingCategoryViewModel, ItemTrainingCategoryBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert categories != null;

        adapter.Update(categories);
    }

    public void StartTraining() {
        Intent i = getIntent();
        String trainingId = i.getStringExtra(TRAINING);
        Intent ni = new Intent(this, SessionActivity.class);

        String json = new Gson().toJson(_model.GetTrainingCategories());

        ni.putExtra(SessionActivity.TrainingExtraTag, trainingId);
        ni.putExtra(SessionActivity.TrainingCategoriesExtraTag, json);

        startActivity(ni);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(TRAINING)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_training_setup);
        _model = new ViewModelProvider(this).get(TrainingSetupViewModel.class);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();
        _binding.tasks.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _model.Load(i.getStringExtra(TRAINING));
    }

    private RecyclerView.Adapter getMyAdapter() {
        MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingTaskViewModel, ItemTrainingTaskBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_training_task);

        MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingTaskViewModel, ItemTrainingTaskBinding>.OnBinded binded =
                adapter.new OnBinded() {

                    @Override
                    public void Binded(ItemTrainingTaskBinding binding,
                                       TrainingSetupViewModel.TrainingTaskViewModel item) {
                        binding.setPresenter(_this);
                        if (binding.categories.getAdapter() == null) {
                            binding.categories.setAdapter(
                                    new MyRecyclerViewAdapter<TrainingSetupViewModel.TrainingCategoryViewModel,
                                            ItemTrainingCategoryBinding>(
                                            _this, R.layout.item_training_category));
                        }
                    }
                };

        adapter.SetOnBinded(binded);
        return adapter;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
