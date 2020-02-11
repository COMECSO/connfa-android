package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Location;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class LocationRequest extends BaseSafeConsumeContainerRequest<Location.Holder> {

    public LocationRequest(DrupalClient client) {
        super(client, new Location.Holder());
    }

    @Override
    protected String getPath() {
        return "getLocations";
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
