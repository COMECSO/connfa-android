package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Event;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class BofsRequest extends BaseSafeConsumeContainerRequest<Event.Holder> {

    public BofsRequest(DrupalClient client) {
        super(client, new Event.Holder());
    }

    @Override
    protected String getPath() {
        return "getBofs";
    }

    @Override
    protected Map<String, String> getItemRequestPostParameters() {
        return null;
    }

    @Override
    protected Map<String, Object> getItemRequestGetParameters(BaseRequest.RequestMethod method) {
        return null;
    }
}
