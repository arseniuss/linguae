package lv.id.arseniuss.linguae.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.DialogBugReportBinding;

public class BugReportDialogFragment extends DialogFragment {
    private DialogBugReportBinding _binding;
    private DialogActionListener _dialogActionListener;

    public BugReportDialogFragment() {
        super();
    }

    public void OnApplicationBugClick() {
        dismiss();
        _dialogActionListener.OnApplicationBug();
    }

    public void OnContentBugClick() {
        dismiss();
        _dialogActionListener.OnContentBug();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_bug_report, null, false);

        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        builder.setView(_binding.getRoot())
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (_dialogActionListener != null) {
                        _dialogActionListener.OnCancelBugReport();
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DialogActionListener) {
            _dialogActionListener = (DialogActionListener) context;
        } else {
            throw new RuntimeException(context + " must implement DialogDismissListener");
        }
    }

    public interface DialogActionListener {
        void OnCancelBugReport();

        void OnApplicationBug();

        void OnContentBug();
    }
}
