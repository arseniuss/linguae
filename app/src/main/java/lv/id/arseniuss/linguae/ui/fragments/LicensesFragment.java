package lv.id.arseniuss.linguae.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentLicensesBinding;
import lv.id.arseniuss.linguae.databinding.ItemLicenseBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.viewmodel.LicenseViewModel;

public class LicensesFragment extends Fragment {
    private LicenseViewModel _model;

    private FragmentLicensesBinding _binding;

    @BindingAdapter("items")
    public static void BindLanguagesList(RecyclerView recyclerView, List<LicenseViewModel.EntryViewModel> entries)
    {
        MyRecyclerViewAdapter<LicenseViewModel.EntryViewModel, ItemLicenseBinding> adapter =
                (MyRecyclerViewAdapter<LicenseViewModel.EntryViewModel, ItemLicenseBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.Update(entries);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _model = new ViewModelProvider(this).get(LicenseViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        _binding = FragmentLicensesBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();

        _binding.licenses.setAdapter(adapter);

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        mainActivity.setSupportActionBar(_binding.toolbar);

        mainActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_licenses, menu);
                mainActivity.SetupDrawer();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {

                return false;
            }
        });

        return _binding.getRoot();
    }

    @NonNull
    private RecyclerView.Adapter getMyAdapter() {

        MyRecyclerViewAdapter<LicenseViewModel.EntryViewModel, ItemLicenseBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_license);

        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}
