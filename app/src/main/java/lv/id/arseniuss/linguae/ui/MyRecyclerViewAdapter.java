package lv.id.arseniuss.linguae.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.BR;
import lv.id.arseniuss.linguae.R;

public class MyRecyclerViewAdapter<TViewModel extends BaseObservable, TDataBinding extends ViewDataBinding>
        extends RecyclerView.Adapter<MyRecyclerViewAdapter<TViewModel, TDataBinding>.MyViewHolder> {
    protected final int _layoutId;
    private final LifecycleOwner _lifecycleOwner;
    protected int _selected = -1;
    private OnSelectionChanged _changed = null;
    private OnLongClickListener _longClick = null;
    private OnBinded _binded = null;
    private List<TViewModel> _items = new ArrayList<>();

    public MyRecyclerViewAdapter(LifecycleOwner lifecycleOwner, int layoutId) {
        _lifecycleOwner = lifecycleOwner;
        _layoutId = layoutId;
    }

    public MyRecyclerViewAdapter(LifecycleOwner lifecycleOwner, int layoutId, Integer selected,
                                 @NonNull OnSelectionChanged callback) {
        _lifecycleOwner = lifecycleOwner;
        _selected = selected;
        _changed = callback;
        _layoutId = layoutId;
    }

    public void SetOnBinded(@NonNull OnBinded callback) {
        _binded = callback;
    }

    public void SetOnLongClickListener(OnLongClickListener listener) {
        _longClick = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TDataBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), _layoutId, parent,
                        false);

        return new MyViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return _items.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.Bind(_lifecycleOwner, _items.get(position), position == _selected, position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void Update(List<TViewModel> viewModels) {
        _items = viewModels;
        notifyDataSetChanged();
    }

    public interface OnSelectionChanged {
        void Changed(int selection);
    }

    public interface OnLongClickListener {
        boolean OnLongClick(int selection);
    }

    public abstract class OnBinded {
        public abstract void Binded(TDataBinding binding, TViewModel item);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TDataBinding _binding;
        View.OnClickListener changeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _selected = getAdapterPosition();
                if (_changed != null) _changed.Changed(_selected);
                notifyItemChanged(_selected);
            }
        };
        private int _position;
        View.OnLongClickListener onLongClickListener = v -> {
            if (_longClick != null) return _longClick.OnLongClick(_position);
            return false;
        };

        public MyViewHolder(@NonNull TDataBinding binding) {
            super(binding.getRoot());
            _binding = binding;

            View view = binding.getRoot().findViewById(R.id.viewButton);
            if (view != null) {
                view.setOnClickListener(changeListener);
                view.setOnLongClickListener(onLongClickListener);
            }
        }

        public void Bind(LifecycleOwner lifecycleOwner, TViewModel item, Boolean checked,
                         int position) {
            if (_binded != null) _binded.Binded(_binding, item);
            _binding.setVariable(BR.viewmodel, item);
            _binding.setLifecycleOwner(lifecycleOwner);
            _position = position;
            item.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    notifyItemChanged(_position);
                }
            });

        }
    }
}
