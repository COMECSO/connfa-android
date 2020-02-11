package com.lemberg.connfa.model.database;

import android.database.sqlite.SQLiteDatabase;

public interface IMigrationTask {
    void onUpgrade(SQLiteDatabase theDb, int oldVersion, int newVersion);
}
