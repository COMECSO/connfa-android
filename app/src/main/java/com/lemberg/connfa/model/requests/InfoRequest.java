package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.data.InfoItem;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class InfoRequest extends BaseSafeConsumeContainerRequest<InfoItem.General> {

    public InfoRequest(DrupalClient client) {
        super(client, new InfoItem.General());
    }

    @Override
    protected String getPath() {
        return "getInfo";
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