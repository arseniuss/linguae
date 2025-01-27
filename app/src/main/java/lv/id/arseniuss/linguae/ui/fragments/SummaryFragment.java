package lv.id.arseniuss.linguae.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentSummaryBinding;
import lv.id.arseniuss.linguae.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.ui.activities.PreferencesActivity;
import lv.id.arseniuss.linguae.viewmodel.SummaryViewModel;

public class SummaryFragment extends Fragment {

    private SummaryViewModel _model;

    private FragmentSummaryBinding _binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _model = new ViewModelProvider(this).get(SummaryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary, container, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        mainActivity.setSupportActionBar(_binding.toolbar);

        mainActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_summary, menu);
                mainActivity.SetupDrawer();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_settings) {
                    startActivity(new Intent(getContext(), PreferencesActivity.class));
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        _model.Load(this::setupLogo);

        return _binding.getRoot();
    }

    private void setupLogo(Bitmap bitmap) {
        _binding.logo.setImageBitmap(bitmap);
    }

}