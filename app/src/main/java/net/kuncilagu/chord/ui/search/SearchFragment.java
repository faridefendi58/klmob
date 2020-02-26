package net.kuncilagu.chord.ui.search;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuncilagu.chord.MainActivity;
import net.kuncilagu.chord.R;
import net.kuncilagu.chord.utils.AppController;
import net.kuncilagu.chord.utils.Server;
import net.kuncilagu.chord.utils.ViewAnimation;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private View root;
    private Context context;

    private EditText et_search;
    private ImageButton bt_clear, bt_back;

    private ProgressBar progress_bar;
    private LinearLayout lyt_no_result;

    private RecyclerView recyclerSuggestion;
    private AdapterSuggestionSearch mAdapterSuggestion;
    private LinearLayout lyt_suggestion;
    private LinearLayout result_container;
    private RecyclerView recyclerResult;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        root = inflater.inflate(R.layout.fragment_search, container, false);
        context = container.getContext();

        initComponent();
        initLatestChord();
        return root;
    }

    private void initComponent() {
        progress_bar = (ProgressBar) root.findViewById(R.id.progress_bar);
        lyt_no_result = (LinearLayout) root.findViewById(R.id.lyt_no_result);

        lyt_suggestion = (LinearLayout) root.findViewById(R.id.lyt_suggestion);
        et_search = (EditText) root.findViewById(R.id.et_search);
        et_search.addTextChangedListener(textWatcher);

        bt_clear = (ImageButton) root.findViewById(R.id.bt_clear);
        bt_back = (ImageButton) root.findViewById(R.id.bt_back);
        bt_clear.setVisibility(View.GONE);
        recyclerSuggestion = (RecyclerView) root.findViewById(R.id.recyclerSuggestion);

        recyclerSuggestion.setLayoutManager(new LinearLayoutManager(context));
        recyclerSuggestion.setHasFixedSize(true);

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSuggestionSearch(context);
        recyclerSuggestion.setAdapter(mAdapterSuggestion);
        ViewAnimation.collapse(lyt_suggestion);

        //showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener(new AdapterSuggestionSearch.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                et_search.setText(viewModel);
                ViewAnimation.collapse(lyt_suggestion);
                hideKeyboard();
                searchAction();
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                bt_back.setImageResource(R.drawable.ic_search_white_24dp);
                hideKeyboard();
                ViewAnimation.collapse(lyt_suggestion);
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSuggestionSearch();
                bt_back.setImageResource(R.drawable.ic_arrow_back);
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        result_container = (LinearLayout) root.findViewById(R.id.result_container);
        recyclerResult = (RecyclerView) root.findViewById(R.id.recyclerResult);
        recyclerResult.setLayoutManager(new LinearLayoutManager(context));
        recyclerResult.setHasFixedSize(true);
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        ViewAnimation.expand(lyt_suggestion);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchAction() {
        progress_bar.setVisibility(View.VISIBLE);
        ViewAnimation.collapse(lyt_suggestion);
        lyt_no_result.setVisibility(View.GONE);

        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            try {
                if (!Server.IS_DEBUG && isNetworkAvailable()) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("limit", "10");
                    params.put("q", query);
                    buildSearchResultList(params);
                } else {
                    dummyData();
                    Toast.makeText(context, getResources().getString(R.string.message_offline_simple), Toast.LENGTH_LONG);
                }
            } catch (Exception e){e.printStackTrace();}
            mAdapterSuggestion.addSearchHistory(query);
        } else {
            Toast.makeText(context, "Please fill search input", Toast.LENGTH_SHORT).show();
            progress_bar.setVisibility(View.GONE);
            lyt_no_result.setVisibility(View.VISIBLE);
        }
    }

    List<SerializableChord> list_chords = new ArrayList<SerializableChord>();

    public void buildSearchResultList(final Map<String, String> params) {
        String url = Server.URL + "chord/search?api-key=" + Server.API_KEY;
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

                                    AdapterListSearch sAdapter = new AdapterListSearch(context , list_chords);
                                    recyclerResult.setAdapter(sAdapter);

                                    sAdapter.setOnItemClickListener(new AdapterListSearch.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, SerializableChord obj, int position) {
                                            Intent newActivity = new Intent(getActivity().getBaseContext(), SearchDetailActivity.class);
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
                                Toast.makeText(getContext(), "Failed!, No data found.",
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

    private void dummyData() {
        String result = Server.DUMMY_DATA;
        try {
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

                AdapterListSearch sAdapter = new AdapterListSearch(context , list_chords);
                recyclerResult.setAdapter(sAdapter);

                sAdapter.setOnItemClickListener(new AdapterListSearch.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, SerializableChord obj, int position) {
                        Intent newActivity = new Intent(getActivity().getBaseContext(), SearchDetailActivity.class);
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
                }, 2000);
            }
        } catch (Exception e){e.printStackTrace();}
    }

    private void initLatestChord() {
        try {
            if (!Server.IS_DEBUG && isNetworkAvailable()) {
                hideKeyboard();

                Map<String, String> params = new HashMap<String, String>();
                params.put("limit", "10");
                params.put("order_by", "t.published_at");
                params.put("cached", "1");
                buildLatestResultList(params);
            } else {
                dummyData();
                Toast.makeText(context, getResources().getString(R.string.message_offline_simple), Toast.LENGTH_LONG);
            }
        } catch (Exception e){e.printStackTrace();}
    }

    List<SerializableChord> list_latest_chords = new ArrayList<SerializableChord>();

    public void buildLatestResultList(final Map<String, String> params) {
        progress_bar.setVisibility(View.VISIBLE);
        ViewAnimation.collapse(lyt_suggestion);
        lyt_no_result.setVisibility(View.GONE);

        String url = Server.URL + "chord/search?api-key=" + Server.API_KEY;
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
                                        list_latest_chords.add(chord);
                                    }
                                    result_container.setVisibility(View.VISIBLE);
                                    lyt_no_result.setVisibility(View.GONE);

                                    AdapterListSearch sAdapter = new AdapterListSearch(context , list_latest_chords);
                                    recyclerResult.setAdapter(sAdapter);

                                    sAdapter.setOnItemClickListener(new AdapterListSearch.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, SerializableChord obj, int position) {
                                            Intent newActivity = new Intent(getActivity().getBaseContext(), SearchDetailActivity.class);
                                            SerializableChord chord = list_latest_chords.get(position);
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
                                progress_bar.setVisibility(View.GONE);
                                lyt_no_result.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
