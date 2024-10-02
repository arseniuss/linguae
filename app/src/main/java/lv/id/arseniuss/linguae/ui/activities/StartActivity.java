package lv.id.arseniuss.linguae.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.BR;
import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.data.LanguageDataParser;
import lv.id.arseniuss.linguae.databinding.ActivityLoadBinding;
import lv.id.arseniuss.linguae.databinding.ActivityPortalBinding;
import lv.id.arseniuss.linguae.ui.AdapterGridLayout;
import lv.id.arseniuss.linguae.ui.MyAdapter;
import lv.id.arseniuss.linguae.viewmodel.StartViewModel;

public class StartActivity extends AppCompatActivity {

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
    public static void BindLanguagesList(AdapterGridLayout adapterGridLayout,
            List<StartViewModel.LanguageViewModel> entries)
    {
        LanguageAdapter adapter = (LanguageAdapter) adapterGridLayout.getAdapter();

        assert adapter != null;
        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _model = new ViewModelProvider(this).get(StartViewModel.class);
        _model.SetWarning(this::onWarning);

        if (_model.HasSelectedLanguage()) {
            ActivityLoadBinding loadBinding = DataBindingUtil.setContentView(this, R.layout.activity_load);

            _binding = loadBinding;
        }
        else {
            ActivityPortalBinding portalBinding = DataBindingUtil.setContentView(this, R.layout.activity_portal);

            portalBinding.portals.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item));
            LanguageAdapter adapter = new LanguageAdapter(this, this, R.layout.item_start_language, position -> {
                if (position >= 0) {
                    Intent i = new Intent(this, StartActivity.class);

                    _model.SetSelectedLanguage(position);

                    startActivity(i);
                    finish();
                }
            });
            portalBinding.languages.setAdapter(adapter);

            _binding = portalBinding;
        }

        _binding.setLifecycleOwner(this);
        _binding.setVariable(BR.viewmodel, _model);
        _binding.setVariable(BR.presenter, this);
    }

    private void onWarning(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        _model.Start(this::Continue, callback -> { });
    }

    public void Continue() {
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }

    public static class LanguageAdapter extends MyAdapter<StartViewModel.LanguageViewModel> {

        public LanguageAdapter(Context context, LifecycleOwner lifecycleOwner, int layout,
                OnItemSelectedListener selectedListener)
        {
            super(context, lifecycleOwner, layout, selectedListener);
        }

        @Override
        public MyAdapter<StartViewModel.LanguageViewModel>.ViewHolder createViewHolder(ViewDataBinding viewDataBinding)
        {
            return new LanguageViewHolder(viewDataBinding);
        }

        public class LanguageViewHolder extends MyAdapter<StartViewModel.LanguageViewModel>.ViewHolder {
            public LanguageViewHolder(ViewDataBinding viewDataBinding) {
                super(viewDataBinding);
            }

            @Override
            public void Bind(int position, StartViewModel.LanguageViewModel languageViewModel) {
                super.Bind(position, languageViewModel);

                ImageView imageView = _binding.getRoot().findViewById(R.id.logo);
                if (imageView != null) {
                    Disposable d = loadImage(languageViewModel.Image).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(imageView::setImageBitmap, Throwable::printStackTrace);
                }
            }

            private Single<Bitmap> loadImage(String imageUrl) {
                return Single.fromCallable(() -> {
                    URL url = new URL(imageUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                });
            }
        }
    }
}
