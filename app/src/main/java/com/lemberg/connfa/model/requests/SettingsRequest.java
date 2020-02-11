package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.SettingsHolder;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class SettingsRequest extends BaseSafeConsumeContainerRequest<SettingsHolder> {

    public SettingsRequest(DrupalClient client) {
        super(client, new SettingsHolder());
    }

    @Override
    protected String getPath() {
        return "getSettings";
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
