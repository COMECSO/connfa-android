package com.lemberg.connfa.ui.drawer;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.managers.SocialManager;

import java.util.List;

public class SocialStrategy implements EventHolderFragmentStrategy {

    private static UpdateRequest request = UpdateRequest.SOCIALS;

    @Override
    public List<Long> getDayList() {
        SocialManager manager = Model.instance().getSocialManager();
        return manager.getSocialsDays();
    }

    @Override
    public int getTextResId() {
        return R.string.placeholder_social_events;
    }

    @Override
    public int getImageResId() {
        return R.drawable.ic_no_social_events;
    }

    @Override
    public boolean updateFavorites() {
        return false;
    }

    @Override
    public boolean update(List<UpdateRequest> requests) {
        return requests.contains(request) || requests.contains(UpdateRequest.SCHEDULES);
    }

    @Override
    public EventMode getEventMode() {
        return EventMode.Social;
    }

    @Override
    public boolean isMySchedule() {
        return false;
    }
}
