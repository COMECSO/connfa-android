package com.lemberg.connfa.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.PreferencesManager;
import com.lemberg.connfa.model.UpdateCallback;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.managers.SharedScheduleManager;
import com.lemberg.connfa.model.managers.ToastManager;
import com.lemberg.connfa.ui.dialog.NoConnectionDialog;
import com.lemberg.drupal.util.L;
import com.lemberg.connfa.util.NetworkUtils;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500;
    private Handler mHandler;

    public static void startThisActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_splash);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Model.instance().getSharedScheduleManager().initialize();
            }
        }).run();

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSplash();
            }
        }, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void startSplash() {
        String lastUpdate = PreferencesManager.getInstance().getLastUpdateDate();
        boolean isOnline = NetworkUtils.isOn(SplashActivity.this);
        if (isOnline) {
            checkForUpdates(this);
        } else if (TextUtils.isEmpty(lastUpdate)) {
            showNoNetworkDialog();
        } else {
            startMainActivity();
        }
    }

    private static void checkForUpdates(final SplashActivity activity) {
        new AsyncTask<Void, Void, UpdatesManager>() {
            @Override
            protected UpdatesManager doInBackground(Void... params) {
                UpdatesManager manager = Model.instance().getUpdatesManager();
                manager.checkForDatabaseUpdate();
                return manager;
            }

            @Override
            protected void onPostExecute(UpdatesManager manager) {
                activity.loadData(manager);
            }
        }.execute();
    }

    private void loadData(UpdatesManager manager) {
        UpdatesManager.startLoading(new UpdateCallback() {
            @Override
            public void onDownloadSuccess() {
                L.d("onDownloadSuccess");
                startMainActivity();
            }

            @Override
            public void onDownloadError() {
                L.d("onDownloadError");
                showNoNetworkDialog();
            }
        }, manager);
    }

    private void startMainActivity() {
        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String uriString = this.getIntent().getDataString();
            Uri uri = Uri.parse(uriString);
            String codeString = uri.getQueryParameter("code");
            if (TextUtils.isEmpty(codeString)) {
                ToastManager.message(this, getString(R.string.url_is_corrupted));
                HomeActivity.startThisActivity(this, SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE);
            } else {
                if (codeString != null) {
                    HomeActivity.startThisActivity(this, Long.parseLong(codeString));
                }
            }
        } else {
            HomeActivity.startThisActivity(this, SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE);
        }

        finish();
    }

    private void showNoNetworkDialog() {
        if (!isFinishing()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(new NoConnectionDialog(), NoConnectionDialog.TAG);
            ft.commitAllowingStateLoss();
        }
    }
}
