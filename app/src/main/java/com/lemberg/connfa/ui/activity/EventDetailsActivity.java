package com.lemberg.connfa.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.widget.NestedScrollView;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.PreferencesManager;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.data.EventDetailsEvent;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.connfa.model.data.Speaker;
import com.lemberg.connfa.model.managers.EventManager;
import com.lemberg.connfa.model.managers.SharedScheduleManager;
import com.lemberg.connfa.model.managers.SpeakerManager;
import com.lemberg.connfa.ui.receiver.ReceiverManager;
import com.lemberg.connfa.ui.view.CircleImageView;
import com.lemberg.connfa.util.DateUtils;
import com.lemberg.connfa.util.ScheduleManager;
import com.lemberg.connfa.util.WebViewUtils;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends StackKeeperActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_DAY = "EXTRA_DAY";
    public static final String EXTRA_HEADER = "EXTRA_HEADER";
    private static boolean showFriendsContainer = true;

    private TextView mToolbarTitle;

    private NestedScrollView mScrollView;
    private MenuItem mItemShare;
    private View mLayoutPlaceholder;

    private EventDetailsEvent mEvent;
    private List<Speaker> mSpeakerList;
    private long mEventId;
    private long mEventStartDate;
    private boolean mIsFavorite;

    private EventDetailsActivity self = this;

    private ReceiverManager receiverManager = new ReceiverManager(
            new ReceiverManager.FavoriteUpdatedListener() {
                @Override
                public void onFavoriteUpdated(long eventId, boolean isFavorite) {
                    mIsFavorite = isFavorite;
                }
            });

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            loadEvent(self);
        }
    };

    public static void startThisActivity(AppCompatActivity activity, long eventId, long day, boolean changeHeader) {
        Intent intent = new Intent(activity, EventDetailsActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        intent.putExtra(EXTRA_DAY, day);
        intent.putExtra(EXTRA_HEADER, changeHeader);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_event_details);

        receiverManager.register(this);
        Model.instance().getUpdatesManager().registerUpdateListener(updateListener);

        initData();
        initToolbar();
        initView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);
        mItemShare = menu.findItem(R.id.actionShare);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        loadEvent(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.actionShare:
                if (mEvent != null) {
                    shareEvent(mEvent.getLink());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiverManager.unregister(this);
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    private void initData() {
        mEventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        mEventStartDate = getIntent().getLongExtra(EXTRA_DAY, -1);
    }

    private void initToolbar() {
        mToolbarTitle = findViewById(R.id.toolbarTitle);

        Toolbar toolbar = findViewById(R.id.toolBar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void initView() {
        mSpeakerList = new ArrayList<>();
    }

    private static void loadEvent(final EventDetailsActivity activity) {
        if (activity.mEventId == -1) return;

        new AsyncTask<Void, Void, EventDetailsEvent>() {
            @Override
            protected EventDetailsEvent doInBackground(Void... params) {
                SpeakerManager speakerManager = Model.instance().getSpeakerManager();
                activity.mSpeakerList.clear();
                activity.mSpeakerList.addAll(speakerManager.getSpeakersByEventId(activity.mEventId));

                EventManager eventManager = Model.instance().getEventManager();
                return eventManager.getEventById(activity.mEventId);
            }

            @Override
            protected void onPostExecute(EventDetailsEvent event) {
                if (event != null) {
                    activity.fillEventView(event);
                }
            }
        }.execute();
    }

    private void fillEventView(@NonNull EventDetailsEvent event) {
        mEvent = event;
        fillToolbar(mEvent);
        fillDate(mEvent);
        fillPreDescription(mEvent);
        fillFavoriteState(mEvent);
        fillSpeakers(mEvent);
        fillDescription(mEvent);
        updatePlaceholderVisibility(mEvent);
        fillFriends();
    }

    private void fillToolbar(@NonNull EventDetailsEvent event) {
        if (mToolbarTitle != null && !TextUtils.isEmpty(event.getEventName())) {
            mToolbarTitle.setText(R.string.event_details);
        }

        if (mItemShare != null && !TextUtils.isEmpty(event.getLink())) {
            mItemShare.setVisible(true);
        }
    }

    private void fillDate(@NonNull EventDetailsEvent event) {
        String eventName = event.getEventName();
        TextView txtEventName = findViewById(R.id.txtEventName);
        txtEventName.setText(eventName);

        String fromTime = DateUtils.getInstance().getTime(this, event.getFrom());
        String toTime = DateUtils.getInstance().getTime(this, event.getTo());
        String eventLocation = DateUtils.getInstance().getWeekDay(mEventStartDate) + ", " + fromTime + " - " + toTime;

        if (!TextUtils.isEmpty(event.getPlace())) {
            String eventPlace = String.format(" en %s", event.getPlace());
            eventLocation += eventPlace;
        }

        TextView txtEventLocation = findViewById(R.id.label_where);
        txtEventLocation.setText(eventLocation);
    }

    private void fillPreDescription(@NonNull EventDetailsEvent event) {
        LinearLayout layoutPreDescription = findViewById(R.id.layoutPreDescription);
        TextView txtTrack = findViewById(R.id.txtTrack);
        TextView txtExpLevel = findViewById(R.id.txtExpLevel);
        ImageView imgExpIcon = findViewById(R.id.imgExperience);

        if (TextUtils.isEmpty(event.getTrack()) && TextUtils.isEmpty(event.getLevel())) {
            layoutPreDescription.setVisibility(View.GONE);
        } else {

            if (!TextUtils.isEmpty(event.getTrack())) {
                txtTrack.setText(event.getTrack());
            } else {
                txtTrack.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(event.getLevel())) {
                txtExpLevel.setText(String.format("%s %s", getString(R.string.exp_level), event.getLevel()));
                imgExpIcon.setImageResource(Level.getIcon(event.getLevelId()));
            } else {
                txtExpLevel.setVisibility(View.GONE);
                imgExpIcon.setVisibility(View.GONE);
            }
        }
    }

    private void fillDescription(@NonNull EventDetailsEvent event) {
        WebView webView = findViewById(R.id.webView);
        if (!TextUtils.isEmpty(event.getDescription())) {

            webView.setVisibility(View.VISIBLE);

            String html = WebViewUtils.getHtml(this, event.getDescription());
            webView.setHorizontalScrollBarEnabled(false);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    completeLoading();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    WebViewUtils.openUrl(EventDetailsActivity.this, url);
                    return true;
                }
            });

        } else {
            webView.setVisibility(View.GONE);
            completeLoading();
        }
    }

    private void updatePlaceholderVisibility(EventDetailsEvent event) {

        mScrollView = findViewById(R.id.scrollView);
        mLayoutPlaceholder = findViewById(R.id.layout_placeholder);

        if (TextUtils.isEmpty(event.getTrack()) &&
                TextUtils.isEmpty(event.getLevel()) &&
                TextUtils.isEmpty(event.getDescription()) &&
                mSpeakerList.isEmpty()) {
            mScrollView.setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mScrollView.setVisibility(View.VISIBLE);
            mLayoutPlaceholder.setVisibility(View.GONE);
        }
    }

    private void fillFavoriteState(@NonNull EventDetailsEvent event) {
        mIsFavorite = event.isFavorite();
        final CheckBox checkBoxFavorite = findViewById(R.id.checkBoxFavorite);
        checkBoxFavorite.setChecked(mIsFavorite);

        RelativeLayout layoutFavorite = findViewById(R.id.layoutFavorite);
        layoutFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxFavorite.setChecked(!checkBoxFavorite.isChecked());
                mIsFavorite = checkBoxFavorite.isChecked();
                setFavorite();
            }
        });
    }

    private void fillFriends() {
        SharedScheduleManager sharedScheduleManager = Model.instance().getSharedScheduleManager();
        ArrayList<String> data = sharedScheduleManager.getSharedSchedulesNamesById(mEventId);

        final LayoutInflater inflater = LayoutInflater.from(EventDetailsActivity.this);
        final LinearLayout holder = findViewById(R.id.friendsHolder);
        holder.removeAllViewsInLayout();

        if (!data.isEmpty()) {
            for (String friend : data) {
                View speakerView = inflater.inflate(R.layout.item_friend, null);
                TextView txtName = speakerView.findViewById(R.id.idFriendName);
                txtName.setText(friend);
                holder.addView(speakerView);
            }
            final ImageView indicator = findViewById(R.id.expandIndicator);
            findViewById(R.id.botFriendsDivider).setVisibility(View.VISIBLE);
            findViewById(R.id.whoIsGoingButton).setVisibility(View.VISIBLE);
            findViewById(R.id.whoIsGoingButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.isShown()) {
                        holder.setVisibility(View.GONE);
                        indicator.setImageResource(R.drawable.ic_group_arrow_down);
                    } else {
                        holder.setVisibility(View.VISIBLE);
                        indicator.setImageResource(R.drawable.ic_group_arrow_up);
                    }

                }
            });

        } else {
            findViewById(R.id.botFriendsDivider).setVisibility(View.GONE);
            findViewById(R.id.whoIsGoingButton).setVisibility(View.GONE);
        }


    }

    private void fillSpeakers(@NonNull EventDetailsEvent event) {
        List<Speaker> speakerList = new ArrayList<>(mSpeakerList);

        LayoutInflater inflater = LayoutInflater.from(EventDetailsActivity.this);
        LinearLayout holderSpeakers = findViewById(R.id.holderSpeakers);
        holderSpeakers.removeAllViewsInLayout();

        if (!speakerList.isEmpty()) {
            for (Speaker speaker : speakerList) {
                View speakerView = inflater.inflate(R.layout.item_speaker_no_letter, null, false);
                fillSpeakerView(speaker, speakerView);
                holderSpeakers.addView(speakerView);
            }
            findViewById(R.id.holderHeader).setVisibility(View.VISIBLE);
            findViewById(R.id.botSpeakersDivider).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.holderHeader).setVisibility(View.GONE);
            findViewById(R.id.botSpeakersDivider).setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void fillSpeakerView(final Speaker speaker, View speakerView) {
        CircleImageView imgPhoto = speakerView.findViewById(R.id.imgPhoto);
        String imageUrl = speaker.getAvatarImageUrl();
        imgPhoto.setImageWithURL(imageUrl);

        TextView txtName = speakerView.findViewById(R.id.txtName);
        txtName.setText(String.format("%s %s", speaker.getFirstName(), speaker.getLastName()));

        String organization = speaker.getOrganization();
        String jobTitle = speaker.getJobTitle();
        String space = (TextUtils.isEmpty(organization) && TextUtils.isEmpty(jobTitle)) || TextUtils.isEmpty(organization) || TextUtils.isEmpty(jobTitle) ? "" : " / ";

        TextView txtJob = speakerView.findViewById(R.id.txtOrgAndJobTitle);
        txtJob.setText(organization + space + jobTitle);

        speakerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventDetailsActivity.this, SpeakerDetailsActivity.class);
                intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, speaker.getId());
                intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER, speaker);
                startActivity(intent);
            }
        });
    }

    private void setFavorite() {
        final SharedScheduleManager sharedScheduleManager = Model.instance().getSharedScheduleManager();

        Model.instance().getSharedScheduleManager().setFavoriteEvent(mEventId, mIsFavorite);
        sharedScheduleManager.postAllSchedules();
        setToNotificationQueue();

        ReceiverManager.updateFavorites(EventDetailsActivity.this, mEventId, mIsFavorite);
    }

    private void setToNotificationQueue() {
        ScheduleManager manager = new ScheduleManager(this);

        if (mIsFavorite) {

            long currMillis = System.currentTimeMillis();
            long eventMillis = mEvent.getFrom();

            if (eventMillis > currMillis) {
                manager.setAlarmForNotification(mEvent, eventMillis, mEventStartDate);
            }

        } else {
            manager.cancelAlarm(mEventId);
        }
    }

    public void shareEvent(String url) {
        startActivity(Intent.createChooser(
                createShareIntent(url),
                getString(R.string.title_share)));
    }

    public Intent createShareIntent(String url) {
        String shareSubject = PreferencesManager.getInstance().getMajorInfoTitle();
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(shareSubject)
                .setText(getString(R.string.body_share) + " " + url);
        return builder.getIntent();
    }

    private void completeLoading() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }



}
