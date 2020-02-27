package net.kuncilagu.chord.controllers;

import android.content.ContentValues;
import android.util.Log;

import net.kuncilagu.chord.utils.Database;
import net.kuncilagu.chord.utils.DatabaseContents;

import java.util.List;

public class ParamsController {
    private static Database database;
    private static ParamsController instance;

    private ParamsController() {

    }

    public static ParamsController getInstance() {
        if (instance == null)
            instance = new ParamsController();

        return instance;
    }

    /**
     * Sets database for use in this class.
     * @param db database.
     */
    public static void setDatabase(Database db) {
        database = db;
    }

    public String getParam(String key) {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_PARAMS + " WHERE name ='"+ key +"'");

        if (contents.isEmpty()) {
            return null;
        }

        ContentValues content = (ContentValues) contents.get(0);
        return content.getAsString("value");
    }

    public Object getParams() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_PARAMS);

        return contents;
    }
}
