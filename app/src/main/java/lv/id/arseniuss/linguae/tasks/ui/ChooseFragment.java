package lv.id.arseniuss.linguae.tasks.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
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

    public ChooseFragment(SessionTaskData current, TaskChangeListener listener) {
        super(current, listener);
    }

    @BindingAdapter("items")
    public static void BindChoiceList(AdapterGridLayout adapterGridLayout,
                                      List<ChooseViewModel.OptionViewModel> entries) {
        OptionAdapter adapter = (OptionAdapter) adapterGridLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(ChooseViewModel.class);
        _model.Load(_task);
        _binding = FragmentTaskChooseBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        OptionAdapter adapter =
                new OptionAdapter(getContext(), getViewLifecycleOwner(),
                        R.layout.item_task_choose_option, position -> {
                    _model.SetSelected(position);
                    if (_listener != null) _listener.OnCanCheckChanged(position >= 0);
                });

        _binding.options.setAdapter(adapter);

        return _binding.getRoot();
    }

    public static class OptionAdapter extends MyAdapter<ChooseViewModel.OptionViewModel> {
        public OptionAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                             OnItemSelectedListener selectedListener) {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<ChooseViewModel.OptionViewModel>.ViewHolder
        createViewHolder(ViewDataBinding viewDataBinding) {
            return new ChoseViewHolder(viewDataBinding);
        }

        public class ChoseViewHolder extends MyAdapter<ChooseViewModel.OptionViewModel>.ViewHolder {
            private final MutableLiveData<Drawable> _background = new MutableLiveData<>(null);

            public ChoseViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);

                _background.setValue(AppCompatResources.getDrawable(_context,
                        R.drawable.radio_button));
            }

            public MutableLiveData<Drawable> Background() {
                return _background;
            }

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                super.onPropertyChanged(sender, propertyId);

                Drawable background;

                if (Boolean.TRUE.equals(_item.IsChecked().getValue())) {
                    if (Boolean.TRUE.equals(_item.IsValid().getValue())) {
                        background = AppCompatResources.getDrawable(_context,
                                R.drawable.button_ok);
                    } else {
                        background = AppCompatResources.getDrawable(_context,
                                R.drawable.radio_button_error);
                    }
                } else {
                    background = AppCompatResources.getDrawable(_context,
                            R.drawable.radio_button);
                }

                _background.setValue(background);
            }

        }
    }
}
