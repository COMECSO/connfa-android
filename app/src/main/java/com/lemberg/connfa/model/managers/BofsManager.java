package com.lemberg.connfa.model.managers;

import com.lemberg.drupal.AbstractBaseDrupalEntity;
import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.PreferencesManager;
import com.lemberg.connfa.model.data.Event;
import com.lemberg.connfa.model.requests.BofsRequest;
import com.lemberg.connfa.ui.adapter.item.EventListItem;
import com.lemberg.connfa.util.DateUtils;

import java.util.Date;
import java.util.List;

public class BofsManager extends EventManager {

    public BofsManager(DrupalClient client) {
        super(client);
    }

    @Override
    protected AbstractBaseDrupalEntity getEntityToFetch(DrupalClient client, Object requestParams) {
        return new BofsRequest(client);
    }

    @Override
    protected String getEntityRequestTag(Object params) {
        return "bofs";
    }

    @Override
    protected boolean storeResponse(Event.Holder requestResponse, String tag) {
        List<Event.Day> bofs = requestResponse.getDays();
        if (bofs == null) {
            return false;
        }

        List<Long> ids = mEventDao.selectFavoriteEventsSafe();
        for (Event.Day day : bofs) {

            for (Event event : day.getEvents()) {
                if (event != null) {

                    Date date = DateUtils.getInstance().convertEventDayDate(day.getDate());
                    if (date != null) {
                        event.setDate(date);
                    }
                    event.setEventClass(Event.BOFS_CLASS);

                    for (long id : ids) {
                        if (event.getId() == id) {
                            event.setFavorite(true);
                            break;
                        }
                    }

                    mEventDao.saveOrUpdateSafe(event);
                    saveEventSpeakers(event);

                    if (event.isDeleted()) {
                        deleteEvent(event);
                    }

                }
            }
        }
        return true;
    }

//    public List<Long> getBofsDays() {
//        return mEventDao.selectDistrictDateSafe(Event.BOFS_CLASS);
//    }

    public List<Long> getBofsDays() {
        List<Long> levelIds = PreferencesManager.getInstance().loadExpLevel();
        List<Long> trackIds = PreferencesManager.getInstance().loadTracks();

        if (levelIds.isEmpty() & trackIds.isEmpty()) {
            return mEventDao.selectDistrictDateSafe(Event.BOFS_CLASS);

        } else if (!levelIds.isEmpty() & !trackIds.isEmpty()) {
            return mEventDao.selectDistrictDateByTrackAndLevelIdsSafe(Event.BOFS_CLASS, levelIds, trackIds);

        } else if (!levelIds.isEmpty() & trackIds.isEmpty()) {
            return mEventDao.selectDistrictDateByLevelIdsSafe(Event.BOFS_CLASS, levelIds);

        } else {
            return mEventDao.selectDistrictDateByTrackIdsSafe(Event.BOFS_CLASS, trackIds);
        }
    }

    public List<EventListItem> getBofsItemsSafe(long day) {
        return mEventDao.selectBofsItemsSafe(Event.BOFS_CLASS, day);
    }
}
