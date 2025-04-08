package lv.id.arseniuss.linguae.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.FragmentLanguageLoadBinding;
import lv.id.arseniuss.linguae.app.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.app.viewmodel.RepositoryLoadViewModel;

public class LanguageLoadFragment extends Fragment {

    private final Boolean _restart;
    private FragmentLanguageLoadBinding _binding;
    private RepositoryLoadViewModel _model;

    public LanguageLoadFragment() {
        super();
        _restart = false;
    }

    public LanguageLoadFragment(boolean restart) {
        super();
        _restart = restart;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _binding = FragmentLanguageLoadBinding.inflate(inflater, container, false);
        _model = new ViewModelProvider(this).get(RepositoryLoadViewModel.class);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        _model.Messages().observe(getViewLifecycleOwner(), this::onMessagesChanged);

        _model.Load(this::Continue, this::requestDatabaseUpdate, _restart);

        return _binding.getRoot();
    }

    private void onMessagesChanged(Spanned spanned) {
        _binding.textMessages.setText(spanned);

        _binding.scrollView.post(() -> _binding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    public void Continue() {
        Intent i = new Intent(requireActivity(), MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }

    private void requestDatabaseUpdate(RepositoryLoadViewModel.ConfirmResponseCallback callback) {
        new MaterialAlertDialogBuilder(requireActivity()).setMessage(R.string.MessageConfirmUpdate)
                .setPositiveButton(R.string.ok, (d, w) -> callback.ConfirmResponse(true))
                .setNegativeButton(R.string.no, (d, w) -> callback.ConfirmResponse(false))
                .show();
    }
}
