package com.lemberg.connfa.model.managers;

import com.lemberg.drupal.AbstractBaseDrupalEntity;
import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.PostResponse;

public class TestManager extends SynchronousItemManager<PostResponse, Object, String> {
    public TestManager(DrupalClient client) {
        super(client);
    }

    @Override
    protected AbstractBaseDrupalEntity getEntityToFetch(DrupalClient client, Object requestParams) {
        return null;
    }

    @Override
    protected String getEntityRequestTag(Object params) {
        return null;
    }

    @Override
    protected boolean storeResponse(PostResponse requestResponse, String tag) {
        return false;
    }
}
