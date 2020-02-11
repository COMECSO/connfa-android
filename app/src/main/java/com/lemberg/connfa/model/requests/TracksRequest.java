package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Track;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class TracksRequest extends BaseSafeConsumeContainerRequest<Track.Holder> {

    public TracksRequest(DrupalClient client) {
        super(client, new Track.Holder());
    }

    @Override
    protected String getPath() {
        return "getTracks";
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
