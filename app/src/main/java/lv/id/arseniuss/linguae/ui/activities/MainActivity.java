package lv.id.arseniuss.linguae.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityMainBinding;
import lv.id.arseniuss.linguae.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration _appBarConfiguration;
    private ActivityMainBinding _binding;
    private MainViewModel _model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _model = new ViewModelProvider(this).get(MainViewModel.class);
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        DrawerLayout drawer = _binding.drawerLayout;

        _appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_summary, R.id.nav_lessons, R.id.nav_training,
                R.id.nav_theory).setOpenableLayout(drawer).build();
    }

    public void SetupDrawer() {
        NavigationView navigationView = _binding.navView;

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, _appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, _appBarConfiguration) || super.onSupportNavigateUp();
    }
}