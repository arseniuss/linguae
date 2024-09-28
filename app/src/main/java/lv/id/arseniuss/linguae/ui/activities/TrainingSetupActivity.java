package lv.id.arseniuss.linguae.ui.activities;

import static lv.id.arseniuss.linguae.viewmodel.TrainingSetupViewModel.TrainingConfigViewModel;
import static lv.id.arseniuss.linguae.viewmodel.TrainingSetupViewModel.TrainingConfigViewModel.SelectionViewModel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityTrainingSetupBinding;
import lv.id.arseniuss.linguae.databinding.ItemTrainingConfigBinding;
import lv.id.arseniuss.linguae.ui.AdapterLinearLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.TrainingSetupViewModel;

public class TrainingSetupActivity extends AppCompatActivity {
    public static final String TRAINING = "TRAINING_ID";
    private final TrainingSetupActivity _this = this;
    private TrainingSetupViewModel _model;
    private ActivityTrainingSetupBinding _binding;

    @BindingAdapter("items")
    public static void BindTrainingList(RecyclerView recyclerView, List<TrainingConfigViewModel> trainings)
    {
        MyRecyclerViewAdapter<TrainingConfigViewModel, ItemTrainingConfigBinding> adapter
                = (MyRecyclerViewAdapter<TrainingConfigViewModel, ItemTrainingConfigBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert trainings != null;

        adapter.Update(trainings);
    }

    @BindingAdapter("items")
    public static void BindSelectionList(AdapterLinearLayout adapterLinearLayout,
            List<TrainingConfigViewModel.SelectionViewModel> entries)
    {
        SelectionAdapter adapter = (SelectionAdapter) adapterLinearLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
        adapter.notifyDataSetChanged();
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
        _binding.categories.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _model.Load(i.getStringExtra(TRAINING));
    }

    private RecyclerView.Adapter getMyAdapter() {
        MyRecyclerViewAdapter<TrainingConfigViewModel, ItemTrainingConfigBinding> adapter = new MyRecyclerViewAdapter<>(
                this, R.layout.item_training_config);

        MyRecyclerViewAdapter<TrainingConfigViewModel, ItemTrainingConfigBinding>.OnBinded binded
                = adapter.new OnBinded() {

            @Override
            public void Binded(ItemTrainingConfigBinding binding, TrainingConfigViewModel item) {
                SelectionAdapter caterogyAdapter = new SelectionAdapter(_this, _this, R.layout.item_training_selection,
                        null);
                SelectionAdapter descriptionAdapter = new SelectionAdapter(_this, _this,
                        R.layout.item_training_selection, null);

                binding.categories.setAdapter(caterogyAdapter);
                binding.descriptions.setAdapter(descriptionAdapter);
                binding.setPresenter(_this);
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

    public static class SelectionAdapter extends MyAdapter<SelectionViewModel> {

        public SelectionAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                OnItemSelectedListener selectedListener)
        {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<TrainingConfigViewModel.SelectionViewModel>.ViewHolder createViewHolder(
                ViewDataBinding viewDataBinding)
        {
            return new SelectionViewHolder(viewDataBinding);
        }

        public class SelectionViewHolder extends MyAdapter<SelectionViewModel>.ViewHolder {

            public SelectionViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
            }
        }
    }
}
