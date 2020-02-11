package com.lemberg.connfa.ui.activity;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import androidx.appcompat.app.AppCompatActivity;

import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdatesManager;

@SuppressLint("Registered")
public class StateActivity extends AppCompatActivity {

    private static boolean wasInBackground = false;

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            wasInBackground = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasInBackground) {
            wasInBackground = false;
            checkForUpdates();
        }
    }

    private void checkForUpdates() {
        UpdatesManager.startLoading(null, Model.instance().getUpdatesManager());
    }
}
