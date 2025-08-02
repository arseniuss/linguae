package lv.id.arseniuss.linguae.app.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.reactivex.rxjava3.disposables.Disposable;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.databinding.DialogBugReportBinding;
import lv.id.arseniuss.linguae.app.db.enumerators.BugLocation;
import lv.id.arseniuss.linguae.app.enumerators.BugDataType;
import lv.id.arseniuss.linguae.app.viewmodel.BugReportViewModel;

public class BugReportDialogFragment extends DialogFragment
        implements BugReportViewModel.OnSaveListener {
    private final BugReportViewModel _model;
    private DialogBugReportBinding _binding;
    private DialogActionListener _dialogActionListener;
    private AlertDialog _dialog;

    public BugReportDialogFragment(AppCompatActivity activity, View rootView) {
        super();

        _model = new ViewModelProvider(activity).get(BugReportViewModel.class);

        Disposable d = Utilities.CaptureScreenshot(rootView).subscribe(this::gotScreenshot);
    }

    private void gotScreenshot(Bitmap bitmap) {
        String base64 = Utilities.BitmapToBase64(bitmap);

        _model.AddData(BugDataType.SCREENSHOT, base64);

        bitmap.recycle();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_bug_report, null, false);


        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);
        _binding.setViewmodel(_model);


        _binding.locationToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.btnApplication && isChecked) {
                _model.SetLocation(BugLocation.APPLICATION_BUG);

            } else if (checkedId == R.id.btnContext && isChecked) {
                _model.SetLocation(BugLocation.TASK_BUG);
            }

            onReportChanged();
        });
        _binding.locationToggleGroup.check(R.id.btnApplication);

        builder.setNegativeButton(R.string.cancel,
                ((dialog, which) -> _dialogActionListener.OnBugReportDismissed()));

        builder.setPositiveButton(R.string.save, ((dialog, which) -> {
            _model.Save(this);
            _dialogActionListener.OnBugReportDismissed();
        }));

        builder.setView(_binding.getRoot());

        _dialog = builder.create();

        _model.Text().observe(this, text -> {
            onReportChanged();
        });

        return _dialog;
    }

    private void onReportChanged() {
        BugLocation location = _model.GetLocation();
        String text = _model.Text().getValue();

        assert text != null;

        if (_dialog != null) _dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled((location == BugLocation.TASK_BUG ||
                        location == BugLocation.APPLICATION_BUG) && !text.isEmpty());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DialogActionListener) {
            _dialogActionListener = (DialogActionListener) context;
        } else {
            throw new RuntimeException(context + " must implement DialogActionListener");
        }
    }

    public void AddData(BugDataType bugDataType, String json) {
        _model.AddData(bugDataType, json);
    }

    @Override
    public void OnBugReportSaved() {
        Toast.makeText(getContext(), R.string.TextBugReportSaved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnBugReportError(Throwable throwable) {
        Toast.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    public interface DialogActionListener {
        void OnBugReportDismissed();
    }
}
