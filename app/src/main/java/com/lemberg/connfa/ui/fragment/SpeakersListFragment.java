package com.lemberg.connfa.ui.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.data.Speaker;
import com.lemberg.connfa.model.managers.SpeakerManager;
import com.lemberg.connfa.ui.activity.SpeakerDetailsActivity;
import com.lemberg.connfa.ui.adapter.SpeakersAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SpeakersListFragment extends Fragment
        implements AdapterView.OnItemClickListener, SpeakersAdapter.OnFilterChangeListener {

    public static final String TAG = "SpeakersFragment";
    private View mLayoutContent, mLayoutPlaceholder;
    private SpeakersAdapter mSpeakersAdapter;
    private ListView mListView;
    private TextView mTxtNoSearchResult;

    private String lastSearchRequest;

    private ProgressBar mProgressBar;

    private SpeakersListFragment self = this;

    private UpdatesManager.DataUpdatedListener updateListener = new UpdatesManager.DataUpdatedListener() {
        @Override
        public void onDataUpdated( List<UpdateRequest> requests) {
            initView(self);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_speakers, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Model.instance().getUpdatesManager().registerUpdateListener(updateListener);
        initView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Model.instance().getUpdatesManager().unregisterUpdateListener(updateListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        initSearchMenuItem(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initSearchMenuItem(Menu menu) {
        SearchManager searchManager = (SearchManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SEARCH_SERVICE);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.actionSearch));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchedText) {
                if (mSpeakersAdapter != null) {
                    lastSearchRequest = searchedText;
                    mSpeakersAdapter.getFilter().filter(searchedText);
                }
                return true;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSpeakersAdapter != null) {
            startSpeakersDetailsActivity(mSpeakersAdapter.getItem(position));
        }
    }

    private static void initView(final SpeakersListFragment activity) {
        if (activity.getView() == null) {
            return;
        }

        activity.mLayoutContent = activity.getView().findViewById(R.id.layout_content);
        activity.mLayoutPlaceholder = activity.getView().findViewById(R.id.layout_placeholder);
        activity.mProgressBar = activity.getView().findViewById(R.id.progressBar);
        activity.mTxtNoSearchResult = activity.getView().findViewById(R.id.txtSearchEmpty);
        activity.mListView = activity.getView().findViewById(R.id.listSpeakers);

        new AsyncTask<Void, Void, List<Speaker>>() {
            @Override
            protected List<Speaker> doInBackground(Void... params) {
                SpeakerManager manager = Model.instance().getSpeakerManager();
                return manager.getSpeakers();
            }

            @Override
            protected void onPostExecute(List<Speaker> speakers) {
                activity.updateView(speakers);
            }
        }.execute();
    }

    private void updateView(final List<Speaker> speakers) {
        if (isDetached() || getView() == null) {
            return;
        }

        if (speakers == null || speakers.isEmpty()) {
            mLayoutContent.setVisibility(View.GONE);
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mLayoutContent.setVisibility(View.VISIBLE);
            mLayoutPlaceholder.setVisibility(View.GONE);

            SparseBooleanArray letterPositions = generateFirstLetterPositions(speakers);
            if (mSpeakersAdapter == null) {
                mSpeakersAdapter = new SpeakersAdapter(getActivity(), speakers, letterPositions);
                mSpeakersAdapter.setOnFilterChangeListener(SpeakersListFragment.this);
                mListView.setOnItemClickListener(SpeakersListFragment.this);
                mListView.setAdapter(mSpeakersAdapter);
            } else {
                mSpeakersAdapter.setData(speakers, letterPositions);
            }

            if (!TextUtils.isEmpty(lastSearchRequest)) {
                mSpeakersAdapter.getFilter().filter(lastSearchRequest);
            } else {
                mSpeakersAdapter.notifyDataSetChanged();
            }
        }
        mProgressBar.setVisibility(View.GONE);
    }

    public static SparseBooleanArray generateFirstLetterPositions(List<Speaker> speakers) {
        SparseBooleanArray positions = new SparseBooleanArray();
        int i = 0;
        String firstNameLetter = "";

        for (Speaker speaker : speakers) {
            String letter = speaker.getFirstName().substring(0, 1).toUpperCase();
            if (firstNameLetter.equals("") || !firstNameLetter.equals(letter)) {
                firstNameLetter = letter;
                positions.put(i, true);
            }
            i++;
        }
        return positions;
    }

    private void startSpeakersDetailsActivity(Speaker speaker) {
        Intent intent = new Intent(getActivity(), SpeakerDetailsActivity.class);
        intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER_ID, speaker.getId());
        intent.putExtra(SpeakerDetailsActivity.EXTRA_SPEAKER, speaker);
        startActivity(intent);
    }

    @Override
    public void onFilterChange(int position) {
        if (position == 0) {
            mTxtNoSearchResult.setVisibility(View.VISIBLE);
        } else {
            mTxtNoSearchResult.setVisibility(View.GONE);
        }
    }
}
