package lv.id.arseniuss.linguae.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTrainingsBinding;
import lv.id.arseniuss.linguae.databinding.ItemTrainingBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.ui.activities.SessionActivity;
import lv.id.arseniuss.linguae.ui.activities.TrainingSetupActivity;
import lv.id.arseniuss.linguae.viewmodel.TrainingsViewModel;
import lv.id.arseniuss.linguae.viewmodel.TrainingsViewModel.EntryViewModel;

public class TrainingsFragment extends Fragment {

    private final TrainingsFragment _this = this;
    private FragmentTrainingsBinding _binding;
    private TrainingsViewModel _model;

    @BindingAdapter("items")
    public static void BindTrainingList(RecyclerView recyclerView, List<EntryViewModel> trainings) {
        MyRecyclerViewAdapter<EntryViewModel, ItemTrainingBinding> adapter
                = (MyRecyclerViewAdapter<EntryViewModel, ItemTrainingBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert trainings != null;

        adapter.Update(trainings);
    }

    public void OnSettingsClicked(EntryViewModel entryViewModel) {
        Intent i = new Intent(getContext(), TrainingSetupActivity.class);

        i.putExtra(TrainingSetupActivity.TRAINING, entryViewModel.getId());

        startActivity(i);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _model = new ViewModelProvider(this).get(TrainingsViewModel.class);
        _binding = FragmentTrainingsBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();
        _binding.trainings.setAdapter(adapter);

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        mainActivity.setSupportActionBar(_binding.toolbar);

        mainActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_trainings, menu);
                mainActivity.SetupDrawer();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                return false;
            }
        });

        return _binding.getRoot();
    }

    @NonNull
    private RecyclerView.Adapter getMyAdapter() {

        MyRecyclerViewAdapter<EntryViewModel, ItemTrainingBinding> adapter = new MyRecyclerViewAdapter<>(this,
                R.layout.item_training, 0, selection -> {

            String trainingId = _model.GetTraining(selection);
            Intent i = new Intent(getContext(), SessionActivity.class);

            i.putExtra(SessionActivity.TrainingExtraTag, trainingId);

            startActivity(i);
        });

        MyRecyclerViewAdapter<EntryViewModel, ItemTrainingBinding>.OnBinded binded = adapter.new OnBinded() {

            @Override
            public void Binded(ItemTrainingBinding binding, EntryViewModel item) {
                binding.setPresenter(_this);
            }
        };

        adapter.SetOnBinded(binded);

        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}