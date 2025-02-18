package lv.id.arseniuss.linguae.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityRepoEditBinding;
import lv.id.arseniuss.linguae.databinding.ItemLanguageRepoBinding;
import lv.id.arseniuss.linguae.ui.fragments.EditRepoDialogFragment;
import lv.id.arseniuss.linguae.viewmodel.RepoEditViewModel;

public class RepoEditActivity extends AppCompatActivity {

    public static final String DATA_ARRAY_JSON = "DATA_ARRAY_JSON";
    public static final String TITLE = "TITLE";
    public static final String SELECT = "SELECT";

    protected RepoEditViewModel _model;

    @BindingAdapter("items")
    public static void BindLanguageRepoList(RecyclerView recyclerView,
                                            List<RepoEditViewModel.EditRepoViewModel> repos) {
        RepoEditAdapter adapter = (RepoEditAdapter) recyclerView.getAdapter();

        assert adapter != null;

        adapter.Update(repos);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (i.hasExtra(TITLE)) actionBar.setTitle(getString(i.getIntExtra(TITLE, 0)));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        ActivityRepoEditBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_repo_edit);
        _model = new ViewModelProvider(this).get(RepoEditViewModel.class);

        binding.setViewmodel(_model);
        binding.setLifecycleOwner(this);

        binding.repos.setAdapter(new RepoEditAdapter(_model.Selected().getValue()));

        Integer selectedIndex = null;

        if (i.hasExtra(SELECT)) {
            selectedIndex = i.getIntExtra(SELECT, 0);
        }

        _model.SetData(i.getStringExtra(DATA_ARRAY_JSON), selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_repo_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.SaveRepositoriesTitle)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        setData();
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        finish();
                    })
                    .show();

            return true;
        } else if (item.getItemId() == R.id.add) {
            Intent i = getIntent();
            EditRepoDialogFragment editRepoDialogFragment = new EditRepoDialogFragment();

            editRepoDialogFragment.SetOnSaveListener((name, location) -> {
                _model.Add(new RepoEditViewModel.EditRepoViewModel(name, location));
            });

            editRepoDialogFragment.show(getSupportFragmentManager(), "test");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setData() {
        Intent i = new Intent();
        String data = _model.GetData();

        i.putExtra(DATA_ARRAY_JSON, data);

        setResult(RESULT_OK, i);
    }

    public static class RepoEditAdapter
            extends RecyclerView.Adapter<RepoEditAdapter.ItemLanguageRepoViewHolder> {
        protected int _selected = -1;
        private List<RepoEditViewModel.EditRepoViewModel> _items = new ArrayList<>();

        public RepoEditAdapter(Integer selected) {
            _selected = selected;
        }

        @NonNull
        @Override
        public ItemLanguageRepoViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
            ItemLanguageRepoBinding binding =
                    ItemLanguageRepoBinding.inflate(LayoutInflater.from(parent.getContext()),
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

        public void Update(List<RepoEditViewModel.EditRepoViewModel> repos) {
            _items = repos;
            notifyDataSetChanged();
        }

        public interface OnSelectionChanged {
            void Changed(int selection);
        }

        public class ItemLanguageRepoViewHolder extends RecyclerView.ViewHolder
                implements EditRepoDialogFragment.OnSaveListener {
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
                        notifyDataSetChanged();
                    }
                };

                _binding.radioLanguage.setOnClickListener(l);
            }

            public void OnDeleteClick() {
                if (_position >= 0 && _position < _items.size()) {
                    _items.remove(_position);
                    notifyItemRemoved(_position);
                }
            }

            public void OnEditClick() {
                RepoEditViewModel.EditRepoViewModel editRepoViewModel = _items.get(_position);
                EditRepoDialogFragment fragment =
                        new EditRepoDialogFragment(editRepoViewModel.Name().getValue(),
                                editRepoViewModel.Location().getValue());
                AppCompatActivity context = (AppCompatActivity) _binding.editButton.getContext();

                fragment.SetOnSaveListener(this);

                fragment.show(context.getSupportFragmentManager(), "fragment-" + _position);
            }

            public void Bind(RepoEditViewModel.EditRepoViewModel item, Boolean checked,
                             int position) {
                _binding.setViewmodel(item);
                _binding.setPresenter(this);
                _binding.radioLanguage.setChecked(checked);
                _position = position;
                item.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        notifyItemChanged(_position);
                    }
                });
            }

            @Override
            public void Confirmed(String name, String location) {
                RepoEditViewModel.EditRepoViewModel editRepoViewModel = _items.get(_position);

                editRepoViewModel.Name().setValue(name);
                editRepoViewModel.Location().setValue(location);

                notifyItemChanged(_position);
            }
        }

    }
}