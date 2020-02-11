package com.lemberg.connfa.model.dao;

import com.lemberg.connfa.model.AppDatabaseInfo;
import com.lemberg.connfa.model.data.Level;
import com.lemberg.connfa.model.database.AbstractEntityDAO;


public class LevelDao extends AbstractEntityDAO<Level, Long> {

    public static final String TABLE_NAME = "table_level";

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
    protected Level newInstance() {
        return new Level();
    }

    @Override
    protected String[] getKeyColumns() {
        return new String[0];
    }
}
