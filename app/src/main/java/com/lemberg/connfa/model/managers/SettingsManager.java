package com.lemberg.connfa.model.managers;

import com.lemberg.drupal.AbstractBaseDrupalEntity;
import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.PreferencesManager;
import com.lemberg.connfa.model.data.SettingsHolder;
import com.lemberg.connfa.model.requests.SettingsRequest;
import com.lemberg.connfa.util.DateUtils;

public class SettingsManager extends SynchronousItemManager<SettingsHolder, Object, String> {

    public SettingsManager(DrupalClient client) {
        super(client);
    }

    @Override
    protected AbstractBaseDrupalEntity getEntityToFetch(DrupalClient client, Object requestParams) {
        return new SettingsRequest(client);
    }

    @Override
    protected String getEntityRequestTag(Object params) {
        return "settings";
    }

    @Override
    protected boolean storeResponse(SettingsHolder requestResponse, String tag) {
        String timeZone = requestResponse.getSettings().getTimeZone();
        PreferencesManager.getInstance().saveTimeZone(timeZone);
        DateUtils.getInstance().setTimezone(timeZone);
        return true;
    }
}
