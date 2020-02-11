package com.lemberg.connfa.ui.activity;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.dao.EventDao;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.connfa.model.data.Speaker;
import com.lemberg.connfa.model.data.SpeakerDetailsEvent;
import com.lemberg.connfa.model.managers.SpeakerManager;
import com.lemberg.connfa.ui.view.CircleImageView;
import com.lemberg.connfa.util.DateUtils;
import com.lemberg.connfa.util.WebViewUtils;

import java.util.List;

public class SpeakerDetailsActivity extends StackKeeperActivity implements View.OnClickListener {

    public static final String EXTRA_SPEAKER = "EXTRA_SPEAKER";
    public static final String EXTRA_SPEAKER_ID = "EXTRA_SPEAKER_ID";

    private static final String TWITTER_URL = "https://twitter.com/";
    private static final String TWITTER_APP_URL = "twitter://user?screen_name=";

    private SpeakerManager mSpeakerManager;
    private long mSpeakerId = -1;
    private String mSpeakerName;
    private Speaker mSpeaker;

    private TextView mTitle;
    private NestedScrollView mScrollView;
    private View mLayoutPlaceholder;

    private boolean mIsDataLoaded;
    private boolean mIsWebLoaded;

    private SpeakerDetailsActivity self = this;

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            loadSpeakerFromDb(self);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_speaker_details);

        Model.instance().getUpdatesManager().registerUpdateListener(updateListener);
        mSpeakerManager = Model.instance().getSpeakerManager();

        initData();
        initToolbar();
        initView();
        loadSpeakerFromDb(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        mSpeaker = getIntent().getParcelableExtra(EXTRA_SPEAKER);
        mSpeakerId = getIntent().getLongExtra(EXTRA_SPEAKER_ID, -1);
        if (mSpeaker != null) {
            mSpeakerName = String.format("%s %s", mSpeaker.getFirstName(), mSpeaker.getLastName());
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        mTitle = findViewById(R.id.toolbarTitle);
        mTitle.setText(R.string.speaker_details);
    }

    private void initView() {
        mScrollView = findViewById(R.id.scrollView);
        mLayoutPlaceholder = findViewById(R.id.layout_placeholder);

        if (mSpeaker == null) {
            mScrollView.setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mScrollView.setVisibility(View.VISIBLE);
            mLayoutPlaceholder.setVisibility(View.GONE);
        }
    }

    private static void loadSpeakerFromDb(final SpeakerDetailsActivity activity) {
        if (activity.mSpeakerId == -1) return;

        new AsyncTask<Void, Void, Speaker>() {
            @Override
            protected Speaker doInBackground(Void... params) {
                return activity.mSpeakerManager.getSpeakerById(activity.mSpeakerId);
            }

            @Override
            protected void onPostExecute(Speaker speaker) {
                if (speaker != null) {
                    activity.fillView(speaker);
                } else {
                    activity.finish();
                }
            }
        }.execute();
    }

    private void fillView(Speaker speaker) {
        mSpeaker = speaker;
        fillSpeakerInfo();
        fillSpeakerDescription();
        fillSocialNetworks();
        loadSpeakerEvents(mSpeaker, this);
    }

    private void fillSpeakerInfo() {
        CircleImageView imgPhoto = findViewById(R.id.imgPhoto);
        String imageUrl = mSpeaker.getAvatarImageUrl();
        imgPhoto.setImageWithURL(imageUrl);

        String speakerName = TextUtils.isEmpty(mSpeaker.getFirstName()) ? "" : mSpeaker.getFirstName() + " ";
        speakerName += TextUtils.isEmpty(mSpeaker.getLastName()) ? "" : mSpeaker.getLastName();
        ((TextView) findViewById(R.id.txtSpeakerName)).setText(speakerName);

        if (TextUtils.isEmpty(mSpeaker.getJobTitle()) && TextUtils.isEmpty(mSpeaker.getOrganization())) {
            findViewById(R.id.txtSpeakerPosition).setVisibility(View.GONE);
        } else {
            findViewById(R.id.txtSpeakerPosition).setVisibility(View.VISIBLE);
        }

        TextView jobTxt = findViewById(R.id.txtSpeakerPosition);
        String jobValue = mSpeaker.getJobTitle() + "" + mSpeaker.getOrganization();

        if (TextUtils.isEmpty(mSpeaker.getJobTitle()) || TextUtils.isEmpty(mSpeaker.getOrganization())) {
            jobValue = jobValue.replace("", "");
        }

        jobTxt.setText(jobValue);
    }

    private void fillSpeakerDescription() {
        WebView webView = findViewById(R.id.webView);
        if (!TextUtils.isEmpty(mSpeaker.getCharact())) {

            String html = WebViewUtils.getHtml(this, mSpeaker.getCharact());
            webView.setVisibility(View.VISIBLE);

            webView.setHorizontalScrollBarEnabled(false);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    mIsWebLoaded = true;
                    completeLoading();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    WebViewUtils.openUrl(SpeakerDetailsActivity.this, url);
                    return true;
                }
            });

        } else {
            webView.setVisibility(View.GONE);
            mIsWebLoaded = true;
            completeLoading();
        }
    }

    private void fillSocialNetworks() {
        if (TextUtils.isEmpty(mSpeaker.getTwitterName())) {
            findViewById(R.id.btnTwitter).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btnTwitter).setOnClickListener(this);
        }

        if (TextUtils.isEmpty(mSpeaker.getWebSite())) {
            findViewById(R.id.btnWebsite).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btnWebsite).setOnClickListener(this);
        }
    }

    private static void loadSpeakerEvents(final Speaker speaker, final SpeakerDetailsActivity activity) {
        new AsyncTask<Void, Void, List<SpeakerDetailsEvent>>() {
            @Override
            protected List<SpeakerDetailsEvent> doInBackground(Void... params) {
                EventDao eventDao = Model.instance().getEventManager().getEventDao();
                return eventDao.getEventsBySpeakerId(speaker.getId());
            }

            @Override
            protected void onPostExecute(List<SpeakerDetailsEvent> events) {
                activity.mIsDataLoaded = true;
                activity.addSpeakerEvents(events);
                activity.completeLoading();
                activity.updatePlaceholderVisibility(events);
            }
        }.execute();
    }

    private void addSpeakerEvents(List<SpeakerDetailsEvent> events) {
        LinearLayout layoutEvents = findViewById(R.id.layoutEvents);
        LayoutInflater inflater = LayoutInflater.from(SpeakerDetailsActivity.this);
        layoutEvents.removeAllViews();

        for (SpeakerDetailsEvent event : events) {
            View eventView = inflater.inflate(R.layout.item_speakers_event, null);
            fillEventView(event, eventView);
            layoutEvents.addView(eventView);
        }
    }

    private void fillEventView(final SpeakerDetailsEvent event, View eventView) {
        ((TextView) eventView.findViewById(R.id.txtArticleName)).setText(event.getEventName());
        TextView txtTrack = eventView.findViewById(R.id.txtTrack);
        if (!TextUtils.isEmpty(event.getTrackName())) {
            txtTrack.setText(event.getTrackName());
            txtTrack.setVisibility(View.VISIBLE);
        }

        String weekDay = DateUtils.getInstance().getWeekDay(event.getFrom());
        String fromTime = DateUtils.getInstance().getTime(this, event.getFrom());
        String toTime = DateUtils.getInstance().getTime(this, event.getTo());

        TextView txtWhere = eventView.findViewById(R.id.txtWhere);
        String date = String.format("%s, %s - %s", weekDay, fromTime, toTime);
        txtWhere.setText(date);
        if (!event.getPlace().equals("")) {
            txtWhere.append(String.format(" in %s", event.getPlace()));
        }

        initEventExpLevel(eventView, event);
        eventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetailsActivity.startThisActivity(SpeakerDetailsActivity.this, event.getEventId(), event.getFrom(), true);
            }
        });
    }

    private void initEventExpLevel(View eventView, SpeakerDetailsEvent event) {
        TextView txtExpLevel = eventView.findViewById(R.id.txtExpLevel);
        ImageView imgExpLevel = eventView.findViewById(R.id.imgExpLevel);

        String expLevel = event.getLevelName();
        if (!TextUtils.isEmpty(expLevel)) {

            String expText = String.format("%s %s", getResources().getString(R.string.experience_level), expLevel);
            txtExpLevel.setText(expText);
            txtExpLevel.setVisibility(View.VISIBLE);

            int expIcon = Level.getIcon(expLevel);
            if (expIcon != 0) {
                imgExpLevel.setImageResource(expIcon);
                imgExpLevel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTwitter:
                view.startAnimation(buttonClick);
                String twitterUrl = TWITTER_URL + mSpeaker.getTwitterName();
                openBrowser(twitterUrl);
                break;
            case R.id.btnWebsite:
                view.startAnimation(buttonClick);
                String url = mSpeaker.getWebSite();
                openBrowser(url);
                break;
        }
    }


    private void openBrowser(String url) {
        try {
            Intent getBrowser = new Intent(Intent.ACTION_VIEW);

            getBrowser.setData(Uri.parse("https://stackoverflow.com"));

            List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(getBrowser, 0);

            if (resolveInfoList.size() > 0) {
                ResolveInfo info = resolveInfoList.get(0);
                String browserPackageName = info.activityInfo.packageName;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage(browserPackageName);
                startActivity(intent);
            }else {
                Toast.makeText(this, getString(R.string.no_apps_can_perform_this_action), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.no_apps_can_perform_this_action), Toast.LENGTH_SHORT).show();
        }
    }

    private void completeLoading() {
        if (mIsDataLoaded && mIsWebLoaded) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        }
    }

    private void updatePlaceholderVisibility(List<SpeakerDetailsEvent> events) {
        if (TextUtils.isEmpty(mSpeaker.getTwitterName()) &&
                TextUtils.isEmpty(mSpeaker.getWebSite()) &&
                TextUtils.isEmpty(mSpeaker.getCharact()) &&
                events.isEmpty()) {
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mLayoutPlaceholder.setVisibility(View.GONE);
        }
    }
}
