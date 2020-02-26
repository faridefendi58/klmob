package net.kuncilagu.chord.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper implements Database {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = DatabaseContents.DATABASE.toString();


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + DatabaseContents.TABLE_ARTISTS + "("

                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT(100),"
                + "slug TEXT(128),"
                + "date_added DATETIME"
                + ");");

        db.execSQL("CREATE TABLE " + DatabaseContents.TABLE_GENRES + "("

                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT(100),"
                + "date_added DATETIME"
                + ");");

        db.execSQL("CREATE TABLE " + DatabaseContents.TABLE_SONGS + "("

                + "_id INTEGER PRIMARY KEY,"
                + "title TEXT(100),"
                + "slug TEXT(128),"
                + "chord_permalink TEXT(128),"
                + "artist_id INTEGER DEFAULT 0,"
                + "genre_id INTEGER DEFAULT 0,"
                + "story TEXT(256),"
                + "content TEXT(256),"
                + "is_favorite INTEGER DEFAULT 0,"
                + "published_at DATETIME,"
                + "date_added DATETIME"
                + ");");

        db.execSQL("CREATE TABLE " + DatabaseContents.TABLE_HISTORY + "("

                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "song_id INTEGER DEFAULT 0,"
                + "viewed_counter INTEGER DEFAULT 0,"
                + "date_added DATETIME,"
                + "date_updated DATETIME"
                + ");");

        db.execSQL("CREATE TABLE " + DatabaseContents.TABLE_PARAMS + "("

                + "_id INTEGER PRIMARY KEY,"
                + "name TEXT(100),"
                + "value TEXT(256),"
                + "type TEXT(16),"
                + "description TEXT(256),"
                + "date_added DATETIME"

                + ");");

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String datetime = dateformat.format(c.getTime());
        db.execSQL("INSERT INTO " + DatabaseContents.TABLE_PARAMS + " (" +
                "_id, name, value, type, description, date_added)\n" +
                "VALUES ('1', 'language', 'in', 'text', '', '"+ datetime +"');");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContents.TABLE_ARTISTS);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContents.TABLE_GENRES);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContents.TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContents.TABLE_HISTORY);

        // Create tables again
        onCreate(db);
    }

    @Override
    public List<Object> select(String queryString) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            List<Object> list = new ArrayList<Object>();
            Cursor cursor = database.rawQuery(queryString, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        ContentValues content = new ContentValues();
                        String[] columnNames = cursor.getColumnNames();
                        for (String columnName : columnNames) {
                            content.put(columnName, cursor.getString(cursor
                                    .getColumnIndex(columnName)));
                        }
                        list.add(content);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
            database.close();
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int insert(String tableName, Object content) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();

            int id = (int) database.insert(tableName, null,
                    (ContentValues) content);

            database.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean update(String tableName, Object content) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues cont = (ContentValues) content;
            // this array will always contains only one element.
            String[] array = new String[]{cont.get("_id")+""};
            database.update(tableName, cont, " _id = ?", array);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String tableName, int id) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(tableName, " _id = ?", new String[]{id+""});
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean execute(String queryString) {
        try{
            SQLiteDatabase database = this.getWritableDatabase();
            database.execSQL(queryString);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAllByAttributes(String tableName, String attr, String val) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(tableName, " "+ attr +" = ?", new String[]{val});
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
