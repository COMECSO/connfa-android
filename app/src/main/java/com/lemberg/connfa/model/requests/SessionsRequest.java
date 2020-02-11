package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Event;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

    public class SessionsRequest extends BaseSafeConsumeContainerRequest<Event.Holder> {

    public SessionsRequest(DrupalClient client) {
        super(client, new Event.Holder());
    }

    @Override
    protected String getPath() {
        return "getSessions";
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

