package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.FloorPlan;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class FloorPlansRequest extends BaseSafeConsumeContainerRequest<FloorPlan.Holder> {

    public FloorPlansRequest(DrupalClient client) {
        super(client, new FloorPlan.Holder());
    }

    @Override
    protected String getPath() {
        return "getFloorPlans";
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
