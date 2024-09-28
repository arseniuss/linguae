package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityFirstBinding;
import lv.id.arseniuss.linguae.viewmodel.FirstViewModel;

public class FirstActivity extends AppCompatActivity {

    private FirstViewModel _model;
    private ActivityFirstBinding _binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_first);
        _model = new ViewModelProvider(this).get(FirstViewModel.class);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);
        _binding.setPresenter(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        _model.Start(this::Continue, callback -> {

        });
    }

    public void Continue() {
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }
}