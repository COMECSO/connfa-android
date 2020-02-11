package com.lemberg.connfa.model.managers;

import com.lemberg.drupal.AbstractBaseDrupalEntity;
import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.dao.TrackDao;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.connfa.model.data.Track;
import com.lemberg.connfa.model.requests.TracksRequest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TracksManager extends SynchronousItemManager<Track.Holder, Object, String> {

    private TrackDao mTrackDao;

    public TracksManager(DrupalClient client) {
        super(client);
        mTrackDao = new TrackDao();
    }

    @Override
    protected AbstractBaseDrupalEntity getEntityToFetch(DrupalClient client, Object requestParams) {
        return new TracksRequest(client);
    }

    @Override
    protected String getEntityRequestTag(Object params) {
        return "tracks";
    }

    @Override
    protected boolean storeResponse(Track.Holder requestResponse, String tag) {
        List<Track> tracks = requestResponse.getTracks();
        if (tracks == null) return false;

        mTrackDao.saveOrUpdateDataSafe(tracks);
        for (Track track : tracks) {
            if (track != null) {
                if (track.isDeleted()) {
                    mTrackDao.deleteDataSafe(track.getId());
                }
            }
        }
        return true;
    }

    public List<Track> getTracks() {
        List<Track> tracks = mTrackDao.getAllSafe();
        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track track, Track track2) {
                return Double.compare(track.getOrder(), track2.getOrder());
            }
        });
        return tracks;
    }

    public Track getTrack(long trackId) {
        List<Track> data = mTrackDao.getDataSafe(trackId);
        return data.size() > 0 ? data.get(0) : null;
    }

    public List<Level> getLevels() {
        LevelsManager levelManager = Model.instance().getLevelsManager();

        List<Level> levels = levelManager.getLevels();
        Collections.sort(levels, new Comparator<Level>() {
            @Override
            public int compare(Level level, Level level2) {
                return Double.compare(level.getOrder(), level2.getOrder());
            }
        });

        return levels;
    }

    public void clear() {
        mTrackDao.deleteAll();
    }
}
