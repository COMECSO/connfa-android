package com.lemberg.connfa.ui.adapter.item;

import com.lemberg.connfa.model.data.Event;

public interface EventItemCreator {

    EventListItem getItem(Event event);

}
