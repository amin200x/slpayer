package com.amin.saazangplayer.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.amin.saazangplayer.R;

public class SettingsActivity extends AppCompatActivity {
    protected static final  String BACKED_FROM_SETTINGS = "backedFromSettings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(R.string.title_activity_settings);
        }

    }

    @Override
    public  boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (MainActivity.getInstance().isSettingsChanged()) {
            Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
            //mainIntent.putExtra(BACKED_FROM_SETTINGS, BACKED_FROM_SETTINGS);
            MainActivity.getInstance().finish();
            startActivity(mainIntent);
        }
        SettingsActivity.this.finish();
       // super.onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_settings_preferences, rootKey);
        }
    }
}