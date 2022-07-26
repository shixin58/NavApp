package com.passion.navapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.passion.navapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navController.navigate(item.getItemId());
        return !TextUtils.isEmpty(item.getTitle());
    }
}