package lv.id.arseniuss.linguae.app.ui.fragments;

import android.content.Intent;
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

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.FragmentTheoriesBinding;
import lv.id.arseniuss.linguae.app.databinding.ItemTheoryBinding;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.app.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.app.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.app.ui.activities.TheoryActivity;
import lv.id.arseniuss.linguae.app.viewmodel.TheoriesViewModel;
import lv.id.arseniuss.linguae.app.viewmodel.TheoriesViewModel.EntryViewModel;

public class TheoriesFragment extends Fragment {
    private final TheoriesFragment _this = this;
    private FragmentTheoriesBinding _binding;
    private TheoriesViewModel _model;

    @BindingAdapter("items")
    public static void BindTrainingList(RecyclerView recyclerView,
                                        List<TheoriesViewModel.EntryViewModel> theories) {
        MyRecyclerViewAdapter<TheoriesViewModel.EntryViewModel, ItemTheoryBinding> adapter =
                (MyRecyclerViewAdapter<EntryViewModel, ItemTheoryBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert theories != null;

        adapter.Update(theories);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(TheoriesViewModel.class);
        _binding = FragmentTheoriesBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();
        _binding.theories.setAdapter(adapter);

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        mainActivity.setSupportActionBar(_binding.toolbar);

        mainActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_theories, menu);
                mainActivity.SetupDrawer();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                return false;
            }
        });

        _binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabName = tab.getText().toString();

                _model.SetSelectedSection(tabName);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return _binding.getRoot();
    }

    @NonNull
    private RecyclerView.Adapter getMyAdapter() {

        MyRecyclerViewAdapter<EntryViewModel, ItemTheoryBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_theory, 0, selection -> {
                    TheoryDataAccess.TheoryWithCount theory = _model.GetTheory(selection);

                    if (theory != null && !theory.Theory.Id.isEmpty() && theory.ChapterCount > 0) {
                        Intent i = new Intent(getContext(), TheoryActivity.class);

                        i.putExtra(TheoryActivity.TheoryExtraTag, theory.Theory.Id);
                        i.putExtra(TheoryActivity.TheoryNameExtraTag, theory.Theory.Title);

                        startActivity(i);
                    }
                });

        MyRecyclerViewAdapter<EntryViewModel, ItemTheoryBinding>.OnBinded binded =
                adapter.new OnBinded() {

                    @Override
                    public void Binded(ItemTheoryBinding binding,
                                       TheoriesViewModel.EntryViewModel item) {
                        binding.setPresenter(_this);
                    }
                };

        adapter.SetOnBinded(binded);

        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}
