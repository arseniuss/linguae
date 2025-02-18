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
import lv.id.arseniuss.linguae.databinding.FragmentTaskDeclineBinding;
import lv.id.arseniuss.linguae.databinding.ItemTaskDeclineCaseBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.DeclineViewModel;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;

public class DeclineFragment extends AbstractTaskFragment<DeclineViewModel> {
    private FragmentTaskDeclineBinding _binding;

    public DeclineFragment(SessionTaskData current, TaskChangeListener listener) {
        super(current, listener);

        if (listener != null) listener.OnCanCheckChanged(true);
    }

    @BindingAdapter("items")
    public static void BindLessonList(RecyclerView recyclerView,
                                      List<DeclineViewModel.CaseViewModel> entries) {
        MyRecyclerViewAdapter<DeclineViewModel.CaseViewModel, ItemTaskDeclineCaseBinding> adapter =
                (MyRecyclerViewAdapter<DeclineViewModel.CaseViewModel, ItemTaskDeclineCaseBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.Update(entries);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(DeclineViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskDeclineBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        MyRecyclerViewAdapter<DeclineViewModel.CaseViewModel, ItemTaskDeclineCaseBinding> adapter =
                getAdapter();

        _binding.cases.setAdapter(adapter);

        return _binding.getRoot();
    }

    @NonNull
    private MyRecyclerViewAdapter<DeclineViewModel.CaseViewModel, ItemTaskDeclineCaseBinding> getAdapter() {
        MyRecyclerViewAdapter<DeclineViewModel.CaseViewModel, ItemTaskDeclineCaseBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_task_decline_case);

        return adapter;
    }
}