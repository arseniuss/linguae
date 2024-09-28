package lv.id.arseniuss.linguae.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityLanguageRepoBinding;
import lv.id.arseniuss.linguae.databinding.ItemLanguageRepoBinding;
import lv.id.arseniuss.linguae.ui.fragments.EditRepoDialogFragment;
import lv.id.arseniuss.linguae.viewmodel.LanguageRepoItemViewModel;
import lv.id.arseniuss.linguae.viewmodel.LanguageRepoViewModel;

public class LanguageRepoActivity extends AppCompatActivity {

    protected LanguageRepoViewModel _model;

    @BindingAdapter("items")
    public static void BindLanguageRepoList(RecyclerView recyclerView, List<LanguageRepoItemViewModel> repos) {
        LanguageRepoAdapter adapter = (LanguageRepoAdapter) recyclerView.getAdapter();

        assert adapter != null;

        adapter.Update(repos);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        ActivityLanguageRepoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_language_repo);
        _model = new ViewModelProvider(this).get(LanguageRepoViewModel.class);

        binding.setViewmodel(_model);
        binding.setLifecycleOwner(this);

        binding.repos.setAdapter(new LanguageRepoAdapter(_model.Selected().getValue(), selection -> {
            _model.Selected(selection);
            finish();
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_repo_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            _model.SaveData();
            this.finish();
            return true;
        }
        else if (item.getItemId() == R.id.add) {
            LanguageRepoItemViewModel model = new LanguageRepoItemViewModel();
            EditRepoDialogFragment editRepoDialogFragment = new EditRepoDialogFragment(model);

            editRepoDialogFragment.SetOnSaveListener(() -> {
                _model.Add(model);
            });

            editRepoDialogFragment.show(getSupportFragmentManager(), "test");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class LanguageRepoAdapter extends RecyclerView.Adapter<LanguageRepoAdapter.ItemLanguageRepoViewHolder> {
        private final OnSelectionChanged _changed;
        protected int _selected = -1;
        private List<LanguageRepoItemViewModel> _items = new ArrayList<>();

        public LanguageRepoAdapter(Integer selected, @NonNull OnSelectionChanged callback) {
            _selected = selected;
            _changed = callback;
        }

        @NonNull
        @Override
        public ItemLanguageRepoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLanguageRepoBinding binding = ItemLanguageRepoBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            return new ItemLanguageRepoViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemLanguageRepoViewHolder holder, int position) {
            holder.Bind(_items.get(position), position == _selected, position);
        }

        @Override
        public int getItemCount() {
            return _items.size();
        }

        public void Update(List<LanguageRepoItemViewModel> repos) {
            _items = repos;
            notifyDataSetChanged();
        }

        public interface OnSelectionChanged {
            void Changed(int selection);
        }

        class ItemLanguageRepoViewHolder extends RecyclerView.ViewHolder {
            private final ItemLanguageRepoBinding _binding;
            private int _position;

            public ItemLanguageRepoViewHolder(@NonNull ItemLanguageRepoBinding binding) {
                super(binding.getRoot());
                _binding = binding;

                View.OnClickListener l = new View.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(View v) {
                        _selected = getAdapterPosition();
                        _changed.Changed(_selected);
                        notifyDataSetChanged();
                    }
                };

                _binding.radioLanguage.setOnClickListener(l);
                _binding.imageButton.setOnClickListener(v -> {
                    EditRepoDialogFragment fragment = new EditRepoDialogFragment(_binding.getViewmodel());

                    fragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "test");
                });
            }

            public void Bind(LanguageRepoItemViewModel item, Boolean checked, int position) {
                _binding.setVariable(lv.id.arseniuss.linguae.BR.viewmodel, item);
                _binding.radioLanguage.setChecked(checked);
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
}