package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class LevelsRequest extends BaseSafeConsumeContainerRequest<Level.Holder> {

    public LevelsRequest(DrupalClient client) {
        super(client, new Level.Holder());
    }

    @Override
    protected String getPath() {
        return "getLevels";
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
