package com.lemberg.connfa.ui.drawer;


import com.lemberg.connfa.model.UpdateRequest;

import java.util.List;

public interface EventHolderFragmentStrategy {
    List<Long> getDayList();

    int getTextResId();

    int getImageResId();

    boolean updateFavorites();

    boolean update(List<UpdateRequest> requests);

    EventMode getEventMode();

    boolean isMySchedule();
}
