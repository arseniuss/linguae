package lv.id.arseniuss.linguae.tasks.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTaskChooseBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.ChooseViewModel;
import lv.id.arseniuss.linguae.ui.AdapterGridLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;

public class ChooseFragment extends AbstractTaskFragment<ChooseViewModel> {

    private FragmentTaskChooseBinding _binding;

    public ChooseFragment(SessionTaskData current) {
        super(current);
    }

    @BindingAdapter("items")
    public static void BindChoiceList(AdapterGridLayout adapterGridLayout,
            List<ChooseViewModel.OptionViewModel> entries)
    {
        OptionAdapter adapter = (OptionAdapter) adapterGridLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        _model = new ViewModelProvider(this).get(ChooseViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskChooseBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        OptionAdapter adapter =
                new OptionAdapter(getContext(), getViewLifecycleOwner(), R.layout.item_task_choose_option, position -> {
                    _model.SetSelected(position);
                });

        _binding.options.setAdapter(adapter);

        return _binding.getRoot();
    }

    public static class OptionAdapter extends MyAdapter<ChooseViewModel.OptionViewModel> {
        public OptionAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                OnItemSelectedListener selectedListener)
        {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<ChooseViewModel.OptionViewModel>.ViewHolder createViewHolder(ViewDataBinding viewDataBinding) {
            return new ChoseViewHolder(viewDataBinding);
        }

        public class ChoseViewHolder extends MyAdapter<ChooseViewModel.OptionViewModel>.ViewHolder {
            public ChoseViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
            }
        }
    }
}
