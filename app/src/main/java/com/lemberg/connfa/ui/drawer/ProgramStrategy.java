package com.lemberg.connfa.ui.drawer;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.managers.ProgramManager;

import java.util.List;

public class ProgramStrategy implements EventHolderFragmentStrategy {

    private static UpdateRequest request = UpdateRequest.PROGRAMS;

    @Override
    public List<Long> getDayList() {
        ProgramManager programManager = Model.instance().getProgramManager();
        return programManager.getProgramDays();
    }

    @Override
    public int getTextResId() {
        return R.string.placeholder_sessions;
    }

    @Override
    public int getImageResId() {
        return R.drawable.ic_no_session;
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
        return EventMode.Program;
    }

    @Override
    public boolean isMySchedule() {
        return false;
    }
}
