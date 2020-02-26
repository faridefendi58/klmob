package net.kuncilagu.chord.ui.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableChord implements Serializable {
    private int id;
    private String title;
    private String slug;
    private String story;
    private String content;
    private String chord_permalink;
    private int artist_id;
    private String artist_name;
    private String artist_slug;
    private int genre_id;
    private String genre_name;
    private String published_at;
    private int is_favorite;

    public SerializableChord(int id, String title, String content, String chord_permalink) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.chord_permalink = chord_permalink;
    }

    /**
     * Returns the description of this LineItem in Map format.
     * @return the description of this LineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id+"");
        map.put("title", title);
        map.put("slug", slug);
        map.put("story", story);
        map.put("content", content);
        map.put("chord_permalink", chord_permalink);
        map.put("artist_id", artist_id+"");
        map.put("artist_name", artist_name);
        map.put("artist_slug", artist_slug);
        map.put("genre_id", genre_id+"");
        map.put("genre_name", genre_name);
        map.put("is_favorite", is_favorite+"");
        map.put("published_at", published_at);

        return map;

    }

    public void setId(int _id) {
        this.id = _id;
    }
    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setStory(String story) {
        this.story = story;
    }
    public String getStory() {
        return story;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getSlug() {
        return slug;
    }

    public void setArtistId(int artist_id) {
        this.artist_id = artist_id;
    }
    public int getArtistId() {
        return artist_id;
    }

    public void setArtistName(String artist_name) {
        this.artist_name = artist_name;
    }
    public String getArtistName() {
        return artist_name;
    }

    public void setArtistSlug(String artist_slug) {
        this.artist_slug = artist_slug;
    }
    public String getArtistSlug() {
        return artist_slug;
    }

    public void setGenreId(int genre_id) {
        this.genre_id = genre_id;
    }
    public int getGenreId() {
        return genre_id;
    }

    public void setGenreName(String genre_name) {
        this.genre_name = genre_name;
    }
    public String getGenreName() {
        return genre_name;
    }

    public void setChordPermalink(String chord_permalink) {
        this.chord_permalink = chord_permalink;
    }
    public String getChordPermalink() {
        return chord_permalink;
    }

    public void setPublishedAt(String published_at) {
        this.published_at = published_at;
    }
    public String getPublishedAt() {
        return published_at;
    }

    public void setIsFavorite(int _is_favorite) {
        this.is_favorite = _is_favorite;
    }
    public int getIsFavorite() {
        return is_favorite;
    }
}

