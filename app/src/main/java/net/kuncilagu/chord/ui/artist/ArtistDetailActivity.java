package net.kuncilagu.chord.ui.artist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuncilagu.chord.R;
import net.kuncilagu.chord.ui.search.AdapterListSearch;
import net.kuncilagu.chord.ui.search.SearchDetailActivity;
import net.kuncilagu.chord.ui.search.SerializableChord;
import net.kuncilagu.chord.utils.AppController;
import net.kuncilagu.chord.utils.Server;
import net.kuncilagu.chord.utils.ViewAnimation;

public class ArtistDetailActivity extends AppCompatActivity {

    private SerializableArtist artist;

    private ProgressBar progress_bar;
    private LinearLayout lyt_no_result;
    private LinearLayout result_container;
    private RecyclerView recyclerResult;

    List<SerializableChord> list_chords = new ArrayList<SerializableChord>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        Intent intent = getIntent();
        if (intent.hasExtra("artist_intent") && intent.hasExtra("artist_intent")) {
            try {
                artist = (SerializableArtist) intent.getExtras().get("artist_intent");
            } catch (Exception e){e.printStackTrace();}
        }

        initToolbar();
        initView();
        initListView();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.green_50), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(artist.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#29a085")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c806d")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(artist.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        lyt_no_result = (LinearLayout) findViewById(R.id.lyt_no_result);
        result_container = (LinearLayout) findViewById(R.id.result_container);

        recyclerResult = (RecyclerView) findViewById(R.id.recyclerResult);
        recyclerResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerResult.setHasFixedSize(true);
    }

    public void buildSearchResultList(final Map<String, String> params) {
        String url = Server.URL + "artist/list-songs?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result data : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                if (success == 1) {
                                    list_chords.clear();
                                    JSONArray data = jObj.getJSONArray("data");
                                    for(int n = 0; n < data.length(); n++) {
                                        JSONObject data_n = data.getJSONObject(n);
                                        SerializableChord chord = new SerializableChord(
                                                data_n.getInt("id"),
                                                data_n.getString("title"),
                                                data_n.getString("chord"),
                                                data_n.getString("chord_permalink")
                                        );

                                        chord.setSlug(data_n.getString("slug"));
                                        chord.setArtistId(data_n.getInt("artist_id"));
                                        chord.setArtistName(data_n.getString("artist_name"));
                                        chord.setArtistSlug(data_n.getString("artist_slug"));
                                        if (data_n.has("story") && data_n.getString("story") != null) {
                                            chord.setStory(data_n.getString("story"));
                                        }
                                        chord.setGenreId(data_n.getInt("genre_id"));
                                        if (data_n.has("genre_name") && data_n.getString("genre_name") != null) {
                                            chord.setGenreName(data_n.getString("genre_name"));
                                        }
                                        chord.setPublishedAt(data_n.getString("published_at"));
                                        list_chords.add(chord);
                                    }
                                    result_container.setVisibility(View.VISIBLE);
                                    lyt_no_result.setVisibility(View.GONE);

                                    AdapterListSearch sAdapter = new AdapterListSearch(getBaseContext() , list_chords);
                                    recyclerResult.setAdapter(sAdapter);

                                    sAdapter.setOnItemClickListener(new AdapterListSearch.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, SerializableChord obj, int position) {
                                            Intent newActivity = new Intent(getBaseContext(), SearchDetailActivity.class);
                                            SerializableChord chord = list_chords.get(position);
                                            newActivity.putExtra("chord_intent", chord);
                                            startActivity(newActivity);
                                        }
                                    });

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress_bar.setVisibility(View.GONE);
                                            lyt_no_result.setVisibility(View.GONE);
                                        }
                                    }, 1000);
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Failed!, No data found.",
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        try {
            AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void initListView() {
        progress_bar.setVisibility(View.VISIBLE);
        lyt_no_result.setVisibility(View.GONE);

        Map<String, String> params = new HashMap<String, String>();
        params.put("artist_slug", artist.getSlug());

        buildSearchResultList(params);
    }
}
