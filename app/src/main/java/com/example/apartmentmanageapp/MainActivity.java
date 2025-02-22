package com.example.apartmentmanageapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        // Set status bar based on Light/Dark Mode
        if ((getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(android.R.color.white));
        } else {
            window.getDecorView().setSystemUiVisibility(0);
            window.setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        // Get NavController from NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // Link BottomNavigationView with NavController
        bottomNavView = findViewById(R.id.bottom_nav_view);
        NavigationUI.setupWithNavController(bottomNavView, navController);

        // Handle any intent extras to select a specific fragment
        handleIntent(getIntent());

        // Handle openFragment extra inside onCreate
        String openFragment = getIntent().getStringExtra("openFragment");
        if (openFragment != null && openFragment.equals("bills")) {
            navController.navigate(R.id.nav_bills);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("targetFragment")) {
            String target = intent.getStringExtra("targetFragment");
            if ("BILLS".equalsIgnoreCase(target) && bottomNavView != null) {
                bottomNavView.setSelectedItemId(R.id.nav_bills);
                intent.removeExtra("targetFragment");
            }
        }
    }
}
