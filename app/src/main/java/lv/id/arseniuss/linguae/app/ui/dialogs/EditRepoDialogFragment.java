package lv.id.arseniuss.linguae.app.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.DialogEditLanguageRepoBinding;

public class EditRepoDialogFragment extends DialogFragment {
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 101;
    private final ViewModel _model;
    private DialogEditLanguageRepoBinding _binding;
    private OnSaveListener _onSaved = null;

    public EditRepoDialogFragment() {
        super();
        _model = new ViewModel();
    }

    public EditRepoDialogFragment(String name, String location) {
        super();
        _model = new ViewModel(name, location);
    }

    public void SetOnSaveListener(OnSaveListener listener) {
        _onSaved = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_edit_language_repo,
                null, false);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        builder.setView(_binding.getRoot())
                .setPositiveButton(R.string.SaveButtonText, (dialog, which) -> {
                    _binding.notifyChange();
                    if (_onSaved != null)
                        _onSaved.Confirmed(_model.Name().getValue(), _model.Location().getValue());
                })
                .setNegativeButton(R.string.CancelButtonText, (dialog, which) -> {
                    Objects.requireNonNull(this.getDialog()).cancel();
                });


        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _binding.setLifecycleOwner(getViewLifecycleOwner());
    }

    public void BrowseFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        startActivityForResult(i, REQUEST_CODE_OPEN_DIRECTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            assert data != null;

            Uri uri = data.getData();

            assert uri != null;

            requireActivity().getContentResolver()
                    .takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            @SuppressLint("UseRequireInsteadOfGet") DocumentFile documentFile =
                    DocumentFile.fromTreeUri(requireContext(), uri);

            if (documentFile != null && documentFile.isDirectory()) {
                DocumentFile[] files = documentFile.listFiles();

                _model.Location().setValue(uri.toString());
            }
        }
    }

    public interface OnSaveListener {
        void Confirmed(String name, String location);
    }

    public static class ViewModel {
        private final MutableLiveData<String> _name = new MutableLiveData<>("");
        private final MutableLiveData<String> _location = new MutableLiveData<>("");

        public ViewModel(String name, String location) {
            _name.setValue(name);
            _location.setValue(location);
        }

        public ViewModel() {

        }

        public MutableLiveData<String> Name() {
            return _name;
        }

        public MutableLiveData<String> Location() {
            return _location;
        }
    }
}
