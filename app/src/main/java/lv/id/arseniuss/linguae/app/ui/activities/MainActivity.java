package lv.id.arseniuss.linguae.app.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import lv.id.arseniuss.linguae.app.Configuration;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.databinding.ActivityMainBinding;
import lv.id.arseniuss.linguae.app.ui.BindingAdapters;
import lv.id.arseniuss.linguae.app.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration _appBarConfiguration;
    private ActivityMainBinding _binding;
    private MainViewModel _model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _model = new ViewModelProvider(this).get(MainViewModel.class);
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        DrawerLayout drawer = _binding.drawerLayout;

        _appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_summary, R.id.nav_lessons,
                        R.id.nav_training, R.id.nav_theory, R.id.nav_vocabulary, R.id.nav_license)
                        .setOpenableLayout(drawer).build();


        View headerView = _binding.navView.getHeaderView(0);

        ImageView imageView = headerView.findViewById(R.id.header_image);
        if (imageView != null) {
            Bitmap bitmap = Configuration.GetLanguageImage();

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                BindingAdapters.SetImageByUrl(imageView, Configuration.GetLanguageImageUrl());
            }
        }
    }

    public void SetupDrawer() {
        NavigationView navigationView = _binding.navView;

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController,
                _appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, _appBarConfiguration) ||
                super.onSupportNavigateUp();
    }
}