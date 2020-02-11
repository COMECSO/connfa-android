package com.lemberg.connfa.ui.drawer;

import com.lemberg.connfa.R;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;

import java.util.List;

public class FavoritesStrategy implements EventHolderFragmentStrategy {

    @Override
    public List<Long> getDayList() {
        return Model.instance().getSharedScheduleManager().getFavoriteEventDays();
    }

    @Override
    public int getTextResId() {
        return R.string.placeholder_schedule;
    }

    @Override
    public int getImageResId() {
        return R.drawable.ic_no_my_schedule;
    }

    @Override
    public boolean updateFavorites() {
        return true;
    }

    @Override
    public boolean update(List<UpdateRequest> requests) {
        return true;
    }

    @Override
    public EventMode getEventMode() {
        return EventMode.Favorites;
    }

    @Override
    public boolean isMySchedule() {
        return true;
    }
}
