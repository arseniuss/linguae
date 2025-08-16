package lv.id.arseniuss.linguae.app.ui.fragments;

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

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.FragmentDocumentationBinding;
import lv.id.arseniuss.linguae.app.databinding.ItemDocumentationBinding;
import lv.id.arseniuss.linguae.app.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.app.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.app.viewmodel.DocumentationViewModel;

public class DocumentationFragment extends Fragment {
    private DocumentationViewModel _model;

    private FragmentDocumentationBinding _binding;

    @BindingAdapter("items")
    public static void BindLanguagesList(RecyclerView recyclerView,
                                         List<DocumentationViewModel.EntryViewModel> entries) {
        MyRecyclerViewAdapter<DocumentationViewModel.EntryViewModel, ItemDocumentationBinding>
                adapter =
                (MyRecyclerViewAdapter<DocumentationViewModel.EntryViewModel, ItemDocumentationBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.Update(entries);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _model = new ViewModelProvider(this).get(DocumentationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _binding = FragmentDocumentationBinding.inflate(inflater, container, false);

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

        MyRecyclerViewAdapter<DocumentationViewModel.EntryViewModel, ItemDocumentationBinding>
                adapter = new MyRecyclerViewAdapter<>(this, R.layout.item_documentation);

        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}
