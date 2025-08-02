package lv.id.arseniuss.linguae.app.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.FragmentRepositorySelectBinding;
import lv.id.arseniuss.linguae.app.databinding.ItemStartLanguageBinding;
import lv.id.arseniuss.linguae.app.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.app.ui.activities.InitialActivity;
import lv.id.arseniuss.linguae.app.ui.activities.RepoEditActivity;
import lv.id.arseniuss.linguae.app.viewmodel.RepositorySelectViewModel;
import lv.id.arseniuss.linguae.entities.Repository;

public class RepositorySelectFragment extends Fragment {
    private static final int REPO_EDIT = 627;

    private final RepositorySelectViewModel _model;
    private final RepositorySelectListener _listener;
    private FragmentRepositorySelectBinding _binding;

    public RepositorySelectFragment(AppCompatActivity activity,
                                    List<Pair<String, Repository>> data,
                                    RepositorySelectListener listener) {
        _listener = listener;
        _model = new ViewModelProvider(activity).get(RepositorySelectViewModel.class);

        _model.SetData(data);
    }

    @BindingAdapter("items")
    public static void BindRepositoriesList(Spinner spinner,
                                            List<RepositorySelectViewModel.RepositoryViewModel> entries) {
        if (entries == null || entries.isEmpty()) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();

        if (adapter == null) {
            adapter = new ArrayAdapter<>(spinner.getContext(),
                    android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);
        }

        adapter.clear();
        adapter.addAll(entries.stream()
                .map(e -> e.Name)
                .collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
    }

    @BindingAdapter("items")
    public static void BindLanguagesList(RecyclerView recyclerView,
                                         List<RepositorySelectViewModel.LanguageViewModel> entries) {
        if (entries == null) return;

        MyRecyclerViewAdapter<RepositorySelectViewModel.LanguageViewModel, ItemStartLanguageBinding>
                adapter =
                (MyRecyclerViewAdapter<RepositorySelectViewModel.LanguageViewModel, ItemStartLanguageBinding>) recyclerView.getAdapter();

        assert adapter != null;

        adapter.Update(entries);
    }

    private MyRecyclerViewAdapter<RepositorySelectViewModel.LanguageViewModel, ItemStartLanguageBinding> getAdapter() {

        return new MyRecyclerViewAdapter<>(this, R.layout.item_start_language, -1, position -> {

            if (position >= 0) {
                _model.LanguageSelected(position);
                _listener.RepositorySelected();
            }
        });
    }

    public void OnEditReposClick() {
        Intent i = new Intent(getContext(), RepoEditActivity.class);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                        requireActivity().getApplication().getBaseContext());

        String json = sharedPreferences.getString(Constants.PreferenceRepositoriesKey, "");

        i.putExtra(RepoEditActivity.DATA_ARRAY_JSON, json);

        startActivityForResult(i, REPO_EDIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REPO_EDIT && resultCode == RESULT_OK) {
            assert data != null;
            String result = data.getStringExtra(RepoEditActivity.DATA_ARRAY_JSON);

            final SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                            requireActivity().getApplication().getBaseContext());

            sharedPreferences.edit()
                    .putString(Constants.PreferenceRepositoriesKey, result)
                    .apply();

            Intent i = new Intent(getContext(), InitialActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(InitialActivity.ACTION, InitialActivity.CHANGE_REPOSITORY);

            startActivity(i);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _binding = FragmentRepositorySelectBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        _model.SelectedRepository().observe(getViewLifecycleOwner(), _model::LoadRepository);

        _binding.languages.setAdapter(getAdapter());

        return _binding.getRoot();
    }

    public interface RepositorySelectListener {
        void RepositorySelected();
    }
}
