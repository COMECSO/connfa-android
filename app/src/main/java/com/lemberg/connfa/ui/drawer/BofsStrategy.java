package com.lemberg.connfa.ui.drawer;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.managers.BofsManager;

import java.util.List;

public class BofsStrategy implements EventHolderFragmentStrategy {

    private static UpdateRequest request = UpdateRequest.BOFS;

    @Override
    public List<Long> getDayList() {
        BofsManager manager = Model.instance().getBofsManager();
        return manager.getBofsDays();
    }

    @Override
    public int getTextResId() {
        return R.string.placeholder_bofs;
    }

    @Override
    public int getImageResId() {
        return R.drawable.ic_no_bofs;
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
        return EventMode.Bofs;
    }

    @Override
    public boolean isMySchedule() {
        return false;
    }
}
