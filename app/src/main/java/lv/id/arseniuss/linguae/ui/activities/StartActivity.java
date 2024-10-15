package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.BR;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.databinding.ActivityLoadBinding;
import lv.id.arseniuss.linguae.databinding.ActivityPortalBinding;
import lv.id.arseniuss.linguae.databinding.ItemStartLanguageBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.StartViewModel;

public class StartActivity extends AppCompatActivity {
    public static final String RESTART = "RESTART";
    private static final int REQUEST_EDIT_REPO = 100;
    private StartViewModel _model;
    private ViewDataBinding _binding;

    @BindingAdapter("items")
    public static void BindPortalsList(Spinner spinner, List<LanguageDataParser.LanguagePortal> entries)
    {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries.stream().map(e -> e.Name).collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
    }

    @BindingAdapter("items")
    public static void BindLanguagesList(RecyclerView recyclerView, List<StartViewModel.LanguageViewModel> entries)
    {
        MyRecyclerViewAdapter<StartViewModel.LanguageViewModel, ItemStartLanguageBinding> adapter =
                (MyRecyclerViewAdapter<StartViewModel.LanguageViewModel, ItemStartLanguageBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.Update(entries);
    }

    private void requestDatabaseUpdate(StartViewModel.ConfirmResponseCallback callback) {
        new MaterialAlertDialogBuilder(this).setMessage(R.string.MessageConfirmUpdate)
                .setPositiveButton(android.R.string.ok, (d, w) -> callback.ConfirmResponse(true))
                .setNegativeButton(android.R.string.no, (d, w) -> callback.ConfirmResponse(false))
                .show();
    }

    public void EditRepoList() {
        Intent i = new Intent(this, RepoEditActivity.class);

        String portalsJson = _model.GetPortalsJson();

        i.putExtra(RepoEditActivity.DATA_ARRAY_JSON, portalsJson);
        i.putExtra(RepoEditActivity.TITLE, R.string.TitleEditPortals);
        i.putExtra(RepoEditActivity.SELECT, _model.SelectedPortal().getValue());

        startActivityForResult(i, REQUEST_EDIT_REPO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_REPO && resultCode == RESULT_OK) {
            assert data != null;
            if (data.hasExtra(RepoEditActivity.DATA_ARRAY_JSON)) {
                String json = data.getStringExtra(RepoEditActivity.DATA_ARRAY_JSON);

                _model.SetPortalsJson(json);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        _model = new ViewModelProvider(this).get(StartViewModel.class);

        if (shouldParseLanguage()) {
            _binding = DataBindingUtil.<ActivityLoadBinding>setContentView(this, R.layout.activity_load);
        }
        else {
            ActivityPortalBinding portalBinding = DataBindingUtil.setContentView(this, R.layout.activity_portal);

            portalBinding.portals.setAdapter(
                    new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item));

            portalBinding.languages.setAdapter(getAdapter());

            _binding = portalBinding;
        }

        _binding.setLifecycleOwner(this);
        _binding.setVariable(BR.viewmodel, _model);
        _binding.setVariable(BR.presenter, this);
    }

    private MyRecyclerViewAdapter<StartViewModel.LanguageViewModel, ItemStartLanguageBinding> getAdapter() {

        return new MyRecyclerViewAdapter<>(this, R.layout.item_start_language, -1, position -> {
            if (position >= 0) {
                Intent ri = new Intent(this, StartActivity.class);

                Pair<String, String> languageData = _model.GetLanguage(position);

                ri.putExtra(Constants.PreferenceLanguageKey, languageData.first);
                ri.putExtra(Constants.PreferenceLanguageUrlKey, languageData.second);

                startActivity(ri);
                finish();
            }
        });
    }

    protected boolean shouldParseLanguage() {
        Intent i = getIntent();

        if (i.hasExtra(RESTART)) return false;

        if (i.hasExtra(Constants.PreferenceLanguageKey) && i.hasExtra(Constants.PreferenceLanguageUrlKey)) return true;

        return _model.HasSelectedLanguage();
    }

    @Override
    protected void onStart() {
        Intent i = getIntent();

        super.onStart();

        if (i.hasExtra(Constants.PreferenceLanguageKey) && i.hasExtra(Constants.PreferenceLanguageUrlKey)) {
            _model.StartLanguageParsing(i.getStringExtra(Constants.PreferenceLanguageKey),
                    i.getStringExtra(Constants.PreferenceLanguageUrlKey), this::Continue, this::requestDatabaseUpdate);
        }
        else if (i.hasExtra(RESTART)) {
            _model.StartPortalLoading();
        }
        else if (_model.HasSelectedLanguage()) {
            _model.StartLanguageParsing(this::Continue, this::requestDatabaseUpdate);
        }
        else {
            _model.StartPortalLoading();
        }
    }

    public void Continue() {
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }

}
