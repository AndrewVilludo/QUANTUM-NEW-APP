package com.example.quantumrealm;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * Post-login screens: action bar back navigation and Logout with confirmation.
 */
public abstract class LogoutAppCompatActivity extends AppCompatActivity {

    private DrawerLayout appDrawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    /** Override to hide the up/back arrow on root screens (e.g. after login). */
    protected boolean showNavigateUp() {
        return true;
    }

    /** Call after {@code setContentView} when the layout includes {@code app_drawer_layout}. */
    protected void setupAppDrawer() {
        appDrawerLayout = findViewById(R.id.app_drawer_layout);
        NavigationView navView = findViewById(R.id.app_nav_view);
        if (appDrawerLayout == null || navView == null) {
            return;
        }
        AppDrawerHelper.bind(this, appDrawerLayout, navView, this::confirmLogout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                appDrawerLayout,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close);
        appDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            boolean showHome = appDrawerLayout != null || showNavigateUp();
            getSupportActionBar().setDisplayHomeAsUpEnabled(showHome);
            getSupportActionBar().setDisplayShowHomeEnabled(showHome);
        }
        NavigationView navView = findViewById(R.id.app_nav_view);
        if (navView != null) {
            AppDrawerHelper.highlightCurrentDestination(this, navView);
        }
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (findViewById(R.id.app_drawer_layout) != null) {
            getMenuInflater().inflate(R.menu.mute_only_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.logout_menu, menu);
        }
        Music.bindMuteMenuItem(this, menu.findItem(R.id.action_mute_music));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Music.bindMuteMenuItem(this, menu.findItem(R.id.action_mute_music));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (appDrawerLayout != null) {
            if (appDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                appDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                appDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Music.handleMuteMenuItem(this, item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout_confirm_yes, (dialog, which) -> AuthHelper.logout(this))
                .setNegativeButton(R.string.logout_confirm_no, null)
                .show();
    }
}
