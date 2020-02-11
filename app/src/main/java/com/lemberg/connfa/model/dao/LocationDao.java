package com.lemberg.connfa.model.dao;

import com.lemberg.connfa.model.AppDatabaseInfo;
import com.lemberg.connfa.model.data.Location;
import com.lemberg.connfa.model.database.AbstractEntityDAO;


public class LocationDao extends AbstractEntityDAO<Location, Long> {

    public static final String TABLE_NAME = "table_location";

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
    protected Location newInstance() {
        return new Location();
    }

    @Override
    protected String[] getKeyColumns() {
        return new String[0];
    }
}
