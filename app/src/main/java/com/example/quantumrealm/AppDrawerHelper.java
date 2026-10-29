package com.example.quantumrealm;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * Side navigation drawer for main post-login hubs (Learning Galaxy, profile, etc.).
 */
public final class AppDrawerHelper {

    private AppDrawerHelper() {
    }

    public static void bind(
            @NonNull AppCompatActivity activity,
            @NonNull DrawerLayout drawerLayout,
            @NonNull NavigationView navigationView,
            @NonNull Runnable onLogout) {
        populateHeader(activity, navigationView);
        highlightCurrentDestination(activity, navigationView);

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            if (handleSelection(activity, item, onLogout)) {
                highlightCurrentDestination(activity, navigationView);
                return true;
            }
            return false;
        });
    }

    private static void populateHeader(AppCompatActivity activity, NavigationView navigationView) {
        if (navigationView.getHeaderCount() == 0) {
            return;
        }
        TextView nameView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        TextView emailView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        if (nameView != null) {
            nameView.setText(AuthHelper.getUserName(activity));
        }
        if (emailView != null) {
            emailView.setText(R.string.nav_header_subtitle);
        }
    }

    static void highlightCurrentDestination(AppCompatActivity activity, NavigationView navigationView) {
        int checkedId = R.id.nav_galaxy;
        if (activity instanceof Journey) {
            checkedId = R.id.nav_galaxy;
        } else if (activity instanceof ProfileActivity) {
            checkedId = R.id.nav_profile;
        } else if (activity instanceof TitleTiersActivity) {
            checkedId = R.id.nav_title_tiers;
        } else if (activity instanceof AchievementsActivity) {
            checkedId = R.id.nav_achievements;
        }
        navigationView.setCheckedItem(checkedId);
    }

    private static boolean handleSelection(
            AppCompatActivity activity,
            @NonNull MenuItem item,
            @NonNull Runnable onLogout) {
        int id = item.getItemId();
        if (id == R.id.nav_galaxy) {
            if (!(activity instanceof Journey)) {
                Intent intent = new Intent(activity, Journey.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
            return true;
        }
        if (id == R.id.nav_profile) {
            if (!(activity instanceof ProfileActivity)) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            }
            return true;
        }
        if (id == R.id.nav_title_tiers) {
            if (!(activity instanceof TitleTiersActivity)) {
                activity.startActivity(new Intent(activity, TitleTiersActivity.class));
            }
            return true;
        }
        if (id == R.id.nav_achievements) {
            if (!(activity instanceof AchievementsActivity)) {
                activity.startActivity(new Intent(activity, AchievementsActivity.class));
            }
            return true;
        }
        if (id == R.id.nav_logout) {
            onLogout.run();
            return true;
        }
        return false;
    }
}
