package com.example.lab1;
/*
Elisa Perini
KTH, sports technology
HT 2021, CM2001 Mobile applications
Lab1: weather app
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab1.data.DataStorage;
import com.example.lab1.data.JSONParser;
import com.example.lab1.data.MeteoModel;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.lab1.data.MeteoList;
import com.example.lab1.network.Downloader;
import com.example.lab1.viewModel.ConnectivityVM;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class ActivityHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = ActivityHome.class.getSimpleName();

    private static long lastDownload = 0;

    /*------ PARSER & STORAGE --------*/
    private DataStorage mDataStorage;

    /*---------- HOOKS --------------*/
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView textViewNet;

    /*---------- VM --------------*/
    ConnectivityVM connectivityVM;

    // networking variables
    private static boolean isConnected; //=true if connected, otherwise false
    private static final int DOWNLOAD_UPDATE_INTERVAL = 3600000; //ms (update every 1h)
    private static final int NETWORK_CHECK_INTERVAL = 10000; //ms (check network every 10 seconds)
    private final Handler timerHandler = new Handler();

    //check network connection
    // runs in onStart
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplication()
                    .getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            // inform user of internet connection
            if (isConnected) {
                textViewNet.setText(R.string.net);
                connectivityVM.setIsConnected(true);
            } else {
                textViewNet.setText(R.string.nonet);
                connectivityVM.setIsConnected(false);
            }
            timerHandler.postDelayed(this, NETWORK_CHECK_INTERVAL);
            //Log.d(LOG_TAG, "Timer: Is connected? " + isConnected);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------- DATA -------------*/
        connectivityVM = new ViewModelProvider(this).get(ConnectivityVM.class);

        /*------------ HOOKS ---------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.main_toolbar);
        textViewNet = findViewById(R.id.netView);

        /*---------- INIT ----------*/
        setSupportActionBar(toolbar);   // Initialise toolbar
        initNavMenu();                  // Initialise navigation menu
        loadFragment(new FragmentHome()); // load default fragment
    }

    @Override
    protected void onStart() {
        super.onStart();
        lastDownload = 0; //when screen rotates, weather updated from serialization
        timerHandler.postDelayed(timerRunnable, 0);
        //deserialiseData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastDownload = 0;
        timerHandler.removeCallbacks(timerRunnable);
    }

    /*----------- NAV MENU ------------------*/

    private void initNavMenu(){
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        navigationView.setCheckedItem(R.id.nav_home);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings){
            loadFragment(new FragmentHome());
        } else if (id == R.id.nav_graph) {
            Intent intent = new Intent(ActivityHome.this, ActivityGraph.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_container, fragment, "");
        fragmentTransaction.commit();
    }

}