package lv.id.arseniuss.linguae.tasks.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentTaskSelectBinding;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.SelectViewModel;
import lv.id.arseniuss.linguae.ui.AdapterFlexboxLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;

public class SelectFragment extends AbstractTaskFragment<SelectViewModel> {

    private FragmentTaskSelectBinding _binding;

    public SelectFragment(SessionTaskData current, TaskChangeListener listener) {
        super(current, listener);
    }

    @BindingAdapter("items")
    public static void BindWordList(AdapterFlexboxLayout flexboxLayout,
                                    List<SelectViewModel.WordViewModel> entries) {
        MyAdapter<SelectViewModel.WordViewModel> adapter =
                (MyAdapter<SelectViewModel.WordViewModel>) flexboxLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(SelectViewModel.class);
        _model.Load(_task, _listener);
        _binding = FragmentTaskSelectBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        WordAdapter wordAdapter = getAdapter();

        _binding.words.setAdapter(wordAdapter);

        return _binding.getRoot();
    }

    private WordAdapter getAdapter() {
        WordAdapter wordAdapter =
                new WordAdapter(getContext(), getViewLifecycleOwner(),
                        R.layout.item_task_select_word);

        return wordAdapter;
    }

    public static class WordAdapter extends MyAdapter<SelectViewModel.WordViewModel> {

        public WordAdapter(Context context, LifecycleOwner lifecycleOwner, int layout) {
            super(context, lifecycleOwner, layout);
        }

        public WordAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                           OnItemSelectedListener selectedListener) {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<SelectViewModel.WordViewModel>.ViewHolder
        createViewHolder(ViewDataBinding viewDataBinding) {
            return new WordViewHolder(viewDataBinding);
        }

        public class WordViewHolder extends MyAdapter<SelectViewModel.WordViewModel>.ViewHolder {

            public WordViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
            }

            @Override
            public void Bind(int position, SelectViewModel.WordViewModel wordViewModel) {
                super.Bind(position, wordViewModel);

                wordViewModel.SelectedWord().observeForever((s) -> {
                    Spinner spinner = _binding.getRoot().findViewById(R.id.options);

                    if (spinner != null) {
                        String title = wordViewModel.Word + " (" + s + ")";
                        TextView textView = (TextView) spinner.getSelectedView();

                        if (textView != null) {
                            textView.setText(title);
                        }

                        spinner.post(() -> {
                            TextView selectedView = (TextView) spinner.getSelectedView();

                            if (selectedView != null) {
                                float textWidth = selectedView.getPaint()
                                        .measureText(selectedView.getText().toString());

                                int newWidth = (int) (textWidth + selectedView.getPaddingLeft()
                                        + selectedView.getPaddingRight());

                                ViewGroup.LayoutParams params = spinner.getLayoutParams();
                                params.width = newWidth;
                                spinner.setLayoutParams(params);
                            }
                        });
                    }
                });
            }
        }
    }
}
