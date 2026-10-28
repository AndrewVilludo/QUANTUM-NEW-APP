package com.example.quantumrealm;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Post-login screens: action bar back navigation and Logout with confirmation.
 */
public abstract class LogoutAppCompatActivity extends AppCompatActivity {

    /** Override to hide the up/back arrow on root screens (e.g. after login). */
    protected boolean showNavigateUp() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showNavigateUp());
            getSupportActionBar().setDisplayShowHomeEnabled(showNavigateUp());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
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
