package lv.id.arseniuss.linguae.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.BaseObservable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter<TItem extends BaseObservable> extends BaseAdapter {
    private final List<TItem> _items = new ArrayList<>();
    private final LayoutInflater _layoutInflater;
    private final int _layout;
    private final MutableLiveData<Integer> _selectedPosition = new MutableLiveData<>(-1);
    private final LifecycleOwner _lifecycleOwner;

    private final OnItemSelectedListener _selectedListener;

    public MyAdapter(Context context, LifecycleOwner lifecycleOwner, int layout) {
        _lifecycleOwner = lifecycleOwner;
        _layout = layout;
        _layoutInflater = LayoutInflater.from(context);
        _selectedListener = null;
    }

    public MyAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                     OnItemSelectedListener selectedListener) {
        _lifecycleOwner = lifecycleOwner;
        _layout = layout;
        _layoutInflater = LayoutInflater.from(context);
        _selectedListener = selectedListener;
        _selectedPosition.observeForever(this::onSelectedChanged);
    }

    private void onSelectedChanged(Integer selection) {
        if (_selectedListener != null) _selectedListener.OnItemSelected(selection);
    }

    @Override
    public int getCount() {
        return _items.size();
    }

    @Override
    public Object getItem(int position) {
        return _items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ViewHolder createViewHolder(ViewDataBinding viewDataBinding) {
        return new ViewHolder(viewDataBinding);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            ViewDataBinding viewDataBinding = DataBindingUtil.inflate(_layoutInflater, _layout, parent, false);

            viewDataBinding.setLifecycleOwner(_lifecycleOwner);
            holder = createViewHolder(viewDataBinding);
            convertView = viewDataBinding.getRoot();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TItem item = _items.get(position);

        holder.Bind(position, item);
        _selectedPosition.observeForever(holder);

        return convertView;
    }

    public void clear() {
        _items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<TItem> entries) {
        _items.addAll(entries);
        notifyDataSetChanged();
    }

    public interface OnItemSelectedListener {
        void OnItemSelected(int position);
    }

    public class ViewHolder extends Observable.OnPropertyChangedCallback implements Observer<Integer> {
        protected final ViewDataBinding _binding;
        protected final Context _context;
        private final MutableLiveData<Boolean> _isSelected = new MutableLiveData<>(false);
        private int _position;
        protected TItem _item;

        public ViewHolder(ViewDataBinding viewDataBinding) {
            _binding = viewDataBinding;
            _context = viewDataBinding.getRoot().getContext();
        }

        public void Bind(int position, TItem item) {
            _position = position;
            _item = item;
            _binding.setVariable(BR.viewmodel, item);
            _binding.setVariable(BR.presenter, this);

            item.addOnPropertyChangedCallback(this);
        }

        public MutableLiveData<Boolean> IsSelected() {
            return _isSelected;
        }

        public void OnSelected() {
            _selectedPosition.setValue(_position);
        }

        @Override
        public void onChanged(Integer selected) {
            _isSelected.postValue(selected == _position);
        }

        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

        }
    }
}
