package com.lemberg.connfa.model.dao;

import com.lemberg.connfa.model.AppDatabaseInfo;
import com.lemberg.connfa.model.data.SharedEvents;
import com.lemberg.connfa.model.database.AbstractEntityDAO;


public class SharedEventsDao extends AbstractEntityDAO<SharedEvents, Long> {

    public static final String TABLE_NAME = "table_friends_favorite_events";

    @Override
    protected String getSearchCondition() {
        return "_id=?";
    }

    @Override
    protected String[] getSearchConditionArguments(Long theId) {
        return new String[]{theId.toString()};
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getDatabaseName() {
        return AppDatabaseInfo.DATABASE_NAME;
    }

    @Override
    protected SharedEvents newInstance() {
        return new SharedEvents();
    }

    @Override
    protected String[] getKeyColumns() {
        return new String[0];
    }

//    public List<SharedEvents> getFavoritesById(long eventId) {
//        String query = "SELECT * FROM table_friends_favorite_events WHERE _event_id =" + eventId;
//
//        return getDataBySqlQuerySafe(query, null);
//    }


}
