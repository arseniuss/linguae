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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTaskTranslateBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.TranslateViewModel;
import lv.id.arseniuss.linguae.ui.AdapterFlexboxLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;

public class TranslateFragment extends AbstractTaskFragment<TranslateViewModel> {
    private FragmentTaskTranslateBinding _binding;
    private WordAdapter _answerAdapter;
    private WordAdapter _optionsAdapter;

    public TranslateFragment(SessionTaskData current)
    {
        super(current);
    }

    @BindingAdapter("items")
    public static void BindWordList(AdapterFlexboxLayout flexboxLayout, List<TranslateViewModel.WordViewModel> entries)
    {
        WordAdapter adapter = (WordAdapter) flexboxLayout.getAdapter();

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
        _model = new ViewModelProvider(this).get(TranslateViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskTranslateBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        _answerAdapter = new WordAdapter(getContext(), getViewLifecycleOwner(), R.layout.item_task_translate_word,
                position -> OnItemSelected(position, _model.Answers(), _model.Options()));
        _optionsAdapter = new WordAdapter(getContext(), getViewLifecycleOwner(), R.layout.item_task_translate_word,
                position -> OnItemSelected(position, _model.Options(), _model.Answers()));

        _binding.options.setAdapter(_optionsAdapter);
        _binding.answer.setAdapter(_answerAdapter);

        return _binding.getRoot();
    }

    private void OnItemSelected(int position, MutableLiveData<List<TranslateViewModel.WordViewModel>> origin,
            MutableLiveData<List<TranslateViewModel.WordViewModel>> target)
    {
        if (!_model.IsValidated().getValue() && position >= 0 && position < origin.getValue().size()) {
            List<TranslateViewModel.WordViewModel> originValue = origin.getValue();
            List<TranslateViewModel.WordViewModel> targetValue = target.getValue();

            TranslateViewModel.WordViewModel model = originValue.get(position);

            originValue.remove(position);
            targetValue.add(model);

            origin.setValue(originValue);
            target.setValue(targetValue);
        }
    }

    public static class WordAdapter extends MyAdapter<TranslateViewModel.WordViewModel> {

        public WordAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                OnItemSelectedListener selectedListener)
        {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<TranslateViewModel.WordViewModel>.ViewHolder createViewHolder(ViewDataBinding viewDataBinding)
        {
            return new WordViewHolder(viewDataBinding);
        }

        public class WordViewHolder extends MyAdapter<TranslateViewModel.WordViewModel>.ViewHolder {

            public WordViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
            }
        }
    }

}