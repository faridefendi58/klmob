package net.kuncilagu.chord.ui.artist;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableArtist implements Serializable {
    public String slug;
    public String name;

    public SerializableArtist() {
    }

    public SerializableArtist(String slug, String name) {
        this.name = name;
        this.slug = slug;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("slug", slug);
        map.put("name", name);

        return map;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}
