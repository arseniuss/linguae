package lv.id.arseniuss.linguae.tasks.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTaskCasingBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.CasingViewModel;
import lv.id.arseniuss.linguae.ui.AdapterFlexboxLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;

public class CasingFragment extends AbstractTaskFragment<CasingViewModel> {

    private FragmentTaskCasingBinding _binding;
    private CasingFragment.WordAdapter _wordAdapter;


    public CasingFragment(SessionTaskData current)
    {
        super(current);
    }

    @BindingAdapter("items")
    public static void BindWordList(AdapterFlexboxLayout flexboxLayout, List<CasingViewModel.WordViewModel> entries)
    {
        CasingFragment.WordAdapter adapter = (CasingFragment.WordAdapter) flexboxLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        _model = new ViewModelProvider(this).get(CasingViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskCasingBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        _wordAdapter = new CasingFragment.WordAdapter(getContext(), getViewLifecycleOwner(),
                R.layout.item_task_casing_word, position -> { });

        _binding.words.setAdapter(_wordAdapter);

        return _binding.getRoot();
    }

    public static class WordAdapter extends MyAdapter<CasingViewModel.WordViewModel> {

        public WordAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                OnItemSelectedListener selectedListener)
        {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @BindingAdapter("items")
        public static void BindOptionsList(Spinner spinner, List<String> entries)
        {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();

            assert adapter != null;
            assert entries != null;

            adapter.clear();
            adapter.addAll(entries);
            adapter.notifyDataSetChanged();
        }

        @Override
        public MyAdapter<CasingViewModel.WordViewModel>.ViewHolder createViewHolder(ViewDataBinding viewDataBinding)
        {
            return new CasingFragment.WordAdapter.WordViewHolder(viewDataBinding);
        }

        public class WordViewHolder extends MyAdapter<CasingViewModel.WordViewModel>.ViewHolder {
            ArrayAdapter<String> _optionsAdapter;

            public WordViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
                _optionsAdapter = new ArrayAdapter<>(viewDataBinding.getRoot().getContext(),
                        android.R.layout.simple_spinner_item);

                View view = _binding.getRoot().findViewById(R.id.options);

                if (view instanceof Spinner) {
                    Spinner options = (Spinner) view;

                    options.setAdapter(_optionsAdapter);
                }
            }
        }
    }


}