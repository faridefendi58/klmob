package net.kuncilagu.chord.controllers;

import android.content.ContentValues;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import net.kuncilagu.chord.ui.search.SerializableChord;
import net.kuncilagu.chord.utils.Database;
import net.kuncilagu.chord.utils.DatabaseContents;
import net.kuncilagu.chord.utils.DateTimeStrategy;

public class SongController {
    private static Database database;
    private static SongController instance;

    private SongController() {}

    public static SongController getInstance() {
        if (instance == null)
            instance = new SongController();
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
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_SONGS);

        return contents;
    }

    public int addSong(SerializableChord song) {
        ContentValues content = new ContentValues();
        content.put("_id", song.getId());
        content.put("title", song.getTitle());
        content.put("slug", song.getSlug());
        content.put("chord_permalink", song.getChordPermalink());
        content.put("artist_id", song.getArtistId());
        content.put("genre_id", song.getGenreId());
        content.put("story", song.getStory());
        content.put("content", song.getContent());
        content.put("published_at", song.getPublishedAt());
        content.put("date_added", DateTimeStrategy.getCurrentTime());

        int id = database.insert(DatabaseContents.TABLE_SONGS.toString(), content);
        if (id > 0) {
            // add artist
            Boolean has_artist_data = false;
            try {
                List<Object> artists = database.select("SELECT _id FROM " + DatabaseContents.TABLE_ARTISTS + " WHERE _id ="+ song.getArtistId());
                if (artists.size() > 0) {
                    ContentValues c_artist = (ContentValues) artists.get(0);
                    if (c_artist.containsKey("_id")) {
                        has_artist_data = true;
                    }
                }
            } catch (Exception e){e.printStackTrace();}
            if (!has_artist_data) {
                ContentValues content2 = new ContentValues();
                content2.put("_id", song.getArtistId());
                content2.put("name", song.getArtistName());
                content2.put("slug", song.getArtistSlug());
                content2.put("date_added", DateTimeStrategy.getCurrentTime());
                int id2 = database.insert(DatabaseContents.TABLE_ARTISTS.toString(), content2);
            }
            // add genre
            Boolean has_genre_data = false;
            try {
                List<Object> genres = database.select("SELECT _id FROM " + DatabaseContents.TABLE_GENRES +" WHERE _id ="+ song.getGenreId());
                if (genres.size() > 0) {
                    ContentValues c_genre = (ContentValues) genres.get(0);
                    if (c_genre.containsKey("_id")) {
                        has_genre_data = true;
                    }
                }
            } catch (Exception e){e.printStackTrace();}
            if (!has_genre_data) {
                ContentValues content3 = new ContentValues();
                content3.put("_id", song.getGenreId());
                content3.put("name", song.getGenreName());
                content3.put("date_added", DateTimeStrategy.getCurrentTime());
                int id3 = database.insert(DatabaseContents.TABLE_GENRES.toString(), content3);
            }
            Log.e(getClass().getSimpleName(), "has artist data : "+ has_artist_data);
            Log.e(getClass().getSimpleName(), "has genre data : "+ has_genre_data);
            // saved a history for the first time
            try {
                ContentValues content4 = new ContentValues();
                content4.put("song_id", song.getId());
                content4.put("viewed_counter", 1);
                content4.put("date_added", DateTimeStrategy.getCurrentTime());
                content4.put("date_updated", DateTimeStrategy.getCurrentTime());
                int id4 = database.insert(DatabaseContents.TABLE_HISTORY.toString(), content4);
            } catch (Exception e){e.printStackTrace();}
        }

        return id;
    }

    public boolean removeSong(int id) {
        Boolean del = database.delete(DatabaseContents.TABLE_SONGS.toString(), id);
        if (del) {
            Boolean del2 = database.deleteAllByAttributes(DatabaseContents.TABLE_HISTORY.toString(), "song_id", id+"");
        }

        return del;
    }

    public SerializableChord getSong(int id) {
        String queryString = "SELECT t._id AS song_id, t.title, t.content, t.chord_permalink, t.slug, t.artist_id, t.genre_id, t.story, t.published_at, " +
                "a.name AS artist_name, a.slug AS artist_slug, g.name AS genre_name " +
                "FROM " + DatabaseContents.TABLE_SONGS + " t " +
                "LEFT JOIN "+ DatabaseContents.TABLE_ARTISTS +" a ON a._id = t.artist_id " +
                "LEFT JOIN "+ DatabaseContents.TABLE_GENRES +" g ON g._id = t.genre_id " +
                "WHERE t._id ="+ id;
        List<Object> songs = database.select(queryString);
        SerializableChord song = null;
        if (songs != null && songs.size() > 0) {
            ContentValues csong = (ContentValues) songs.get(0);
            if (csong.containsKey("song_id")) {
                song = new SerializableChord(
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
            }
        }

        return song;
    }

    public Boolean hasSong(int song_id) {
        Boolean has_song = false;
        try {
            List<Object> songs = database.select("SELECT _id FROM " + DatabaseContents.TABLE_SONGS + " WHERE _id =" + song_id);
            if (songs != null && songs.size() > 0) {
                has_song = true;
            }
        } catch (Exception e) {e.printStackTrace();}

        return has_song;
    }

    public boolean addToFavorite(int id) {
        Boolean update = false;
        try {
            ContentValues content = new ContentValues();
            content.put("_id", id);
            content.put("is_favorite", 1);
            Log.e(getClass().getSimpleName(), "content : "+ content.toString());

            update = database.update(DatabaseContents.TABLE_SONGS.toString(), content);
        } catch (Exception e) {
            update = false;
            e.printStackTrace();
        }

        return update;
    }

    public List<SerializableChord> getFavorites(int limit) {
        String queryString = "SELECT t._id AS song_id, t.title, t.content, t.chord_permalink, t.slug, t.artist_id, t.genre_id, t.story, t.published_at, " +
                "a.name AS artist_name, a.slug AS artist_slug, g.name AS genre_name " +
                "FROM " + DatabaseContents.TABLE_SONGS + " t " +
                "LEFT JOIN "+ DatabaseContents.TABLE_ARTISTS +" a ON a._id = t.artist_id " +
                "LEFT JOIN "+ DatabaseContents.TABLE_GENRES +" g ON g._id = t.genre_id " +
                "WHERE t.is_favorite = 1 ORDER BY a.name ASC LIMIT "+ limit;
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
