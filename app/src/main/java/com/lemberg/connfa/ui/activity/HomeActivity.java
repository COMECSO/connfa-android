package com.lemberg.connfa.ui.activity;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.connfa.model.data.Track;
import com.lemberg.connfa.model.managers.SharedScheduleManager;
import com.lemberg.connfa.model.managers.TracksManager;
import com.lemberg.connfa.ui.adapter.item.EventListItem;
import com.lemberg.connfa.ui.dialog.FilterDialog;
import com.lemberg.connfa.ui.dialog.IrrelevantTimezoneDialogFragment;
import com.lemberg.connfa.ui.drawer.DrawerAdapter;
import com.lemberg.connfa.ui.drawer.DrawerManager;
import com.lemberg.connfa.ui.drawer.DrawerMenu;
import com.lemberg.connfa.ui.drawer.DrawerMenuItem;
import com.lemberg.connfa.util.KeyboardUtils;
import com.lemberg.connfa.util.L;
import com.lemberg.connfa.util.ScheduleManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeActivity extends StateActivity implements FilterDialog.OnFilterApplied {
    private static final String NAVIGATE_TO_SCHEDULE_EXTRA_KEY = "navigate_to_schedule_extra_key";
    private DrawerManager mFrManager;
    private DrawerAdapter mAdapter;
    private int mPresentTitle;
    private int mSelectedItem = 0;
    private boolean isIntentHandled = false;

    private DrawerLayout mDrawerLayout;

    public FilterDialog mFilterDialog;

    public boolean mIsDrawerItemClicked;

    public HomeActivity self = this;

    private UpdatesManager.DataUpdatedListener updateReceiver = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            initFilterDialog(self);
        }
    };

    public static void startThisActivity(AppCompatActivity activity, long scheduleCode) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(NAVIGATE_TO_SCHEDULE_EXTRA_KEY, scheduleCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            SplashActivity.startThisActivity(this);
        }
        setContentView(R.layout.ac_main);
        Model.instance().getUpdatesManager().registerUpdateListener(updateReceiver);

        long code = getIntent().getLongExtra(NAVIGATE_TO_SCHEDULE_EXTRA_KEY, SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE);
        L.e("Navigate code = " + code);
        initToolbar();

        initNavigationDrawerList();
        initFilterDialog(this);

        initFragmentManager(code);
        if (getIntent().getExtras() != null) {
            isIntentHandled = true;
        }
        handleIntent(getIntent());
        showIrrelevantTimezoneDialogIfNeeded();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(null);
        if (!isIntentHandled) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onDestroy() {
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateReceiver);
        super.onDestroy();
    }

    @Override
    public void onNewFilterApplied() {
        mFrManager.reloadPrograms(DrawerMenu.getNavigationDrawerItems().get(mSelectedItem).getEventMode());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        initNavigationDrawer(toolbar);
    }

    private void initNavigationDrawer(Toolbar toolbar) {
        mDrawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.closeDrawers();
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NotNull View drawerView, float slideOffset) {
                KeyboardUtils.hideKeyboard(getCurrentFocus());
            }

            @Override
            public void onDrawerOpened(@NotNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NotNull View drawerView) {
                if (mIsDrawerItemClicked) {
                    mIsDrawerItemClicked = false;
                    changeFragment();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        drawerToggle.syncState();
    }

    private void initNavigationDrawerList() {
        List<DrawerMenuItem> menu = DrawerMenu.getNavigationDrawerItems();
        mAdapter = new DrawerAdapter(this, menu);

        final ListView listView = findViewById(R.id.leftDrawer);
        if (listView != null) {
            listView.addHeaderView(
                    getLayoutInflater().inflate(R.layout.nav_drawer_header, listView, false),
                    null,
                    false);
        }

        if (listView != null) {
            listView.setAdapter(mAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    onDrawerItemClick(position - listView.getHeaderViewsCount());
                }
            });
        }
    }

    public static void initFilterDialog(final HomeActivity activity) {
        new AsyncTask<Void, Void, List<EventListItem>>() {
            @Override
            protected List<EventListItem> doInBackground(Void... params) {
                TracksManager tracksManager = Model.instance().getTracksManager();
                List<Track> trackList = tracksManager.getTracks();
                List<Level> levelList = tracksManager.getLevels();

                Collections.sort(trackList, new Comparator<Track>() {
                    @Override
                    public int compare(Track track1, Track track2) {
                        String name1 = track1.getName();
                        String name2 = track2.getName();
                        return name1.compareToIgnoreCase(name2);
                    }
                });

                String[] tracks = new String[trackList.size()];
                String[] levels = new String[levelList.size()];

                for (int i = 0; i < trackList.size(); i++) {
                    tracks[i] = trackList.get(i).getName();
                }

                for (int i = 0; i < levelList.size(); i++) {
                    levels[i] = levelList.get(i).getName();
                }

                activity.mFilterDialog = FilterDialog.newInstance(tracks, levels);
                activity.mFilterDialog.setData(levelList, trackList);

                return null;
            }

            @Override
            protected void onPostExecute(List<EventListItem> eventListItems) {}
        }.execute();
    }

    public void closeFilterDialog() {
        if (mFilterDialog != null) {
            if (mFilterDialog.isAdded()) {
                mFilterDialog.dismissAllowingStateLoss();
            }
            mFilterDialog.clearFilter();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent.getExtras() != null) {
            long eventId = intent.getLongExtra(EventDetailsActivity.EXTRA_EVENT_ID, -1);
            long day = intent.getLongExtra(EventDetailsActivity.EXTRA_DAY, -1);
            if (eventId != -1 && day != -1) {
                redirectToDetails(eventId, day);
                isIntentHandled = false;
                new ScheduleManager(this).cancelAlarm(eventId);
            }

        }
    }

    private void redirectToDetails(long id, long day) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, id);
        intent.putExtra(EventDetailsActivity.EXTRA_DAY, day);
        startActivity(intent);
    }

    private void onDrawerItemClick(int position) {
        mDrawerLayout.closeDrawers();
        if (mSelectedItem == position) {
            return;
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mSelectedItem = position;
        mIsDrawerItemClicked = true;
    }

    private void changeFragment() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        {
            if (mFrManager != null) {
                mFrManager.setFragment(DrawerMenu.getNavigationDrawerItems().get(mSelectedItem).getEventMode(), SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE);
                mPresentTitle = DrawerMenu.getNavigationDrawerItems().get(mSelectedItem).getName();

                setToolBarTitle(mPresentTitle);

                mAdapter.setSelectedPos(mSelectedItem);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initFragmentManager(long code) {
        mFrManager = DrawerManager.getInstance(getSupportFragmentManager(), R.id.mainFragment);

        setDefaultFragment(code);
    }

    private void showIrrelevantTimezoneDialogIfNeeded() {
        if (!IrrelevantTimezoneDialogFragment.isCurrentTimezoneRelevant()
                && IrrelevantTimezoneDialogFragment.canPresentMessage(this)
                && !isFinishing()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(new IrrelevantTimezoneDialogFragment(), IrrelevantTimezoneDialogFragment.TAG);
            ft.commitAllowingStateLoss();
        }
    }

    private void setDefaultFragment(long code) {
        DrawerMenuItem drawerMenuItem;
        if (code == SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE) {
            drawerMenuItem = DrawerMenu.getNavigationDrawerItems().get(mSelectedItem);
            mFrManager.setFragment(drawerMenuItem.getEventMode(), SharedScheduleManager.MY_DEFAULT_SCHEDULE_CODE);
        } else {
            mSelectedItem = DrawerMenu.MY_SCHEDULE_POSITION;
            drawerMenuItem = DrawerMenu.getMyScheduleDrawerMenuItem();
            mAdapter.setSelectedPos(mSelectedItem);
            mFrManager.setFragment(drawerMenuItem.getEventMode(), code);
        }
        mPresentTitle = drawerMenuItem.getName();
        setToolBarTitle(mPresentTitle);
    }

    private void setToolBarTitle(int title){
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }
    }


}