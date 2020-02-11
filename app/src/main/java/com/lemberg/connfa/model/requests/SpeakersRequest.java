package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Speaker;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class SpeakersRequest extends BaseSafeConsumeContainerRequest<Speaker.Holder> {

    public SpeakersRequest(DrupalClient client) {
        super(client, new Speaker.Holder());
    }

    @Override
    protected String getPath() {
        return "getSpeakers";
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
