package lv.id.arseniuss.linguae.tasks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTaskConjugateBinding;
import lv.id.arseniuss.linguae.databinding.ItemTaskConjugatePersonBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.ConjugateViewModel;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;

public class ConjugateFragment extends AbstractTaskFragment<ConjugateViewModel> {

    private FragmentTaskConjugateBinding _binding;

    public ConjugateFragment(SessionTaskData current, TaskChangeListener listener) {
        super(current, listener);

        if (listener != null) listener.OnCanCheckChanged(true);
    }

    @BindingAdapter("items")
    public static void BindLessonList(RecyclerView recyclerView,
                                      List<ConjugateViewModel.PersonViewModel> entries) {
        MyRecyclerViewAdapter<ConjugateViewModel.PersonViewModel, ItemTaskConjugatePersonBinding>
                adapter =
                (MyRecyclerViewAdapter<ConjugateViewModel.PersonViewModel, ItemTaskConjugatePersonBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.Update(entries);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(ConjugateViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskConjugateBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        MyRecyclerViewAdapter<ConjugateViewModel.PersonViewModel, ItemTaskConjugatePersonBinding>
                adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_task_conjugate_person);

        _binding.cases.setAdapter(adapter);

        return _binding.getRoot();
    }
}