package com.rescuereach.responder;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationDrawerHandler implements NavigationView.OnNavigationItemSelectedListener {

    private final Activity activity;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final ActionBarDrawerToggle toggle;
    private final FirebaseAuth auth;

    public NavigationDrawerHandler(Activity activity, DrawerLayout drawerLayout, NavigationView navigationView,
                                   ActionBarDrawerToggle toggle, FirebaseAuth auth) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toggle = toggle;
        this.auth = auth;

        setupDrawer();
    }

    private void setupDrawer() {
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            Toast.makeText(activity, "Home Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_incident_report) {
            Toast.makeText(activity, "Incident Report Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_map_view) {
            Toast.makeText(activity, "Map View Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_profile) {
            Toast.makeText(activity, "Profile Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item) || item.getItemId() == android.R.id.home;
    }

    private void logoutUser() {
        auth.signOut();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    public void handleBackPress() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            activity.onBackPressed();
        }
    }
}