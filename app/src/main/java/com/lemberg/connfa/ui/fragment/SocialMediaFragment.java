package com.lemberg.connfa.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.lemberg.connfa.R;
import com.lemberg.connfa.app.App;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.util.NetworkUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SocialMediaFragment extends Fragment {

    public static final String TAG = "SocialMediaFragment";
    private View rootView;
    private View mLayoutPlaceholder;

    private UpdatesManager.DataUpdatedListener updateReceiver = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated(List<UpdateRequest> requests) {
            updateData(requests);
        }
    };


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_social_media, container, false);
        mLayoutPlaceholder = rootView.findViewById(R.id.layout_placeholder);
        return rootView;
    }

    private void updateData(List<UpdateRequest> requestIds) {
        for (UpdateRequest id : requestIds) {
            if (UpdateRequest.SETTINGS == id) {
                fillView();
                break;
            }
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        Model.instance().getUpdatesManager().registerUpdateListener(updateReceiver);

        fillView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void fillView() {

        if (!NetworkUtils.isOn(Objects.requireNonNull(getActivity()))) {
            rootView.findViewById(R.id.web_view).setVisibility(View.GONE);
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.GONE);
            TextView emptyView = rootView.findViewById(R.id.EmptyView);
            emptyView.setText(R.string.NoConnectionMessage);
        }

        WebView webView = rootView.findViewById(R.id.web_view);

        webView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                completeLoading();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);

        String data = "<a class=\"twitter-timeline\" data-theme=\"light\" href=\"" + App.getContext().getString(R.string.twitter_feed_url) +"\"></a> <script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>";

        webView.loadDataWithBaseURL("https://twitter.com", data,
                "text/html", "UTF-8", null);
    }

    private void completeLoading() {
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        rootView.findViewById(R.id.web_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateReceiver);
        super.onDestroyView();
    }
}
