package com.lemberg.connfa.model.requests;

import com.lemberg.drupal.DrupalClient;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.data.Schedule;
import com.lemberg.drupal.http.base.BaseRequest;

import java.util.Map;

public class ScheduleRequest extends BaseScheduleRequest<Schedule.Holder> {

    public ScheduleRequest(DrupalClient client) {
        super(client, new Schedule.Holder());
    }

    @Override
    protected String getPath() {
        return Model.instance().getSharedScheduleManager().getPath();
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