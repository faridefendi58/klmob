package net.kuncilagu.chord.controllers;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

import net.kuncilagu.chord.ui.search.SerializableChord;
import net.kuncilagu.chord.utils.Database;
import net.kuncilagu.chord.utils.DatabaseContents;

public class HistoryController {
    private static Database database;
    private static HistoryController instance;

    private HistoryController() {}

    public static HistoryController getInstance() {
        if (instance == null)
            instance = new HistoryController();
        return instance;
    }

    /**
     * Sets database for use in this class.
     * @param db database.
     */
    public static void setDatabase(Database db) {
        database = db;
    }

    public List<Object> getItems() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_HISTORY);

        return contents;
    }

    public List<SerializableChord> getSongs(int limit) {
        String queryString = "SELECT h.date_updated AS last_viewed_at, t._id AS song_id, t.title, t.content, t.chord_permalink, t.slug, t.artist_id, t.genre_id, t.story, t.published_at, " +
                "a.name AS artist_name, a.slug AS artist_slug, g.name AS genre_name " +
                "FROM " + DatabaseContents.TABLE_HISTORY + " h " +
                "LEFT JOIN "+ DatabaseContents.TABLE_SONGS +" t ON t._id = h.song_id " +
                "LEFT JOIN "+ DatabaseContents.TABLE_ARTISTS +" a ON a._id = t.artist_id " +
                "LEFT JOIN "+ DatabaseContents.TABLE_GENRES +" g ON g._id = t.genre_id " +
                "WHERE 1 ORDER BY h.date_updated DESC LIMIT "+ limit;
        List<Object> songs = database.select(queryString);
        List<SerializableChord> items = new ArrayList<SerializableChord>();
        if (songs != null && songs.size() > 0) {
            for (int i=0; i<songs.size(); i++) {
                ContentValues csong = (ContentValues) songs.get(i);
                if (csong.containsKey("song_id")) {
                    SerializableChord song = new SerializableChord(
                            csong.getAsInteger("song_id"),
                            csong.getAsString("title"),
                            csong.getAsString("content"),
                            csong.getAsString("chord_permalink")
                    );
                    song.setSlug(csong.getAsString("slug"));
                    song.setArtistId(csong.getAsInteger("artist_id"));
                    song.setArtistName(csong.getAsString("artist_name"));
                    song.setArtistSlug(csong.getAsString("artist_slug"));
                    if (csong.containsKey("story") && csong.getAsString("story") != null) {
                        song.setStory(csong.getAsString("story"));
                    }
                    song.setGenreId(csong.getAsInteger("genre_id"));
                    if (csong.containsKey("genre_name") && csong.getAsString("genre_name") != null) {
                        song.setGenreName(csong.getAsString("genre_name"));
                    }
                    song.setPublishedAt(csong.getAsString("published_at"));

                    items.add(song);
                }
            }
        }

        return items;
    }
}
