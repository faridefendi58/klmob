package net.kuncilagu.chord.ui.artist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuncilagu.chord.MainActivity;
import net.kuncilagu.chord.R;
import net.kuncilagu.chord.ui.search.AdapterListSearch;
import net.kuncilagu.chord.ui.search.AdapterSuggestionSearch;
import net.kuncilagu.chord.ui.search.SearchDetailActivity;
import net.kuncilagu.chord.ui.search.SearchFragment;
import net.kuncilagu.chord.ui.search.SerializableChord;
import net.kuncilagu.chord.utils.AppController;
import net.kuncilagu.chord.utils.Server;
import net.kuncilagu.chord.utils.ViewAnimation;

public class ArtistsFragment extends Fragment {

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
    List<SerializableArtist> list_artists = new ArrayList<SerializableArtist>();
    private AdapterListArtist adapterListArtist;
    private String android_id = "0";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_artists, container, false);
        context = container.getContext();

        initComponent();
        initLatestChord();
        return root;
    }

    private void initComponent() {
        android_id = ((MainActivity)context).getAndroidId();

        progress_bar = (ProgressBar) root.findViewById(R.id.progress_bar);
        lyt_no_result = (LinearLayout) root.findViewById(R.id.lyt_no_result);

        lyt_suggestion = (LinearLayout) root.findViewById(R.id.lyt_suggestion);
        et_search = (EditText) root.findViewById(R.id.et_search);
        et_search.addTextChangedListener(textWatcher);

        bt_clear = (ImageButton) root.findViewById(R.id.bt_clear);
        bt_back = (ImageButton) root.findViewById(R.id.bt_back);
        bt_clear.setVisibility(View.GONE);

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                rebuildList(list_artists);
            }
        });

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
                bt_back.setImageResource(R.drawable.ic_search_white_24dp);
                hideKeyboard();
                ViewAnimation.collapse(lyt_suggestion);
                rebuildList(list_artists);
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
                if (list_artists.size() > 0) {
                    List<SerializableArtist> filteredList = new ArrayList<>();
                    for (SerializableArtist row : list_artists) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    if (filteredList.size() > 0) {
                        rebuildList(filteredList);
                    } else {
                        lyt_no_result.setVisibility(View.VISIBLE);
                    }
                    progress_bar.setVisibility(View.GONE);
                }
            } catch (Exception e){e.printStackTrace();}
        } else {
            Toast.makeText(context, "Please fill search input", Toast.LENGTH_SHORT).show();
            progress_bar.setVisibility(View.GONE);
            lyt_no_result.setVisibility(View.VISIBLE);
        }
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final SearchFragment.VolleyCallback callback) {

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
        String result = loadDummyArtists(getContext());
        Log.d(getTag(), "result : "+ result);
        try {
            JSONObject data = new JSONObject(result);
            // Check for error node in json
            if (data.length() > 0) {
                list_artists.clear();
                Iterator<String> keys = data.keys();

                while(keys.hasNext()) {
                    String key = keys.next();
                    SerializableArtist serializableArtist = new SerializableArtist(key, data.getString(key));
                    list_artists.add(serializableArtist);
                }

                result_container.setVisibility(View.VISIBLE);
                lyt_no_result.setVisibility(View.GONE);

                adapterListArtist = new AdapterListArtist(context , list_artists);
                recyclerResult.setAdapter(adapterListArtist);

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
            Boolean is_online = ((MainActivity)context).isNetworkAvailable();
            if (!Server.IS_DEBUG && is_online) {
                hideKeyboard();

                Map<String, String> params = new HashMap<String, String>();
                params.put("limit", "10");
                params.put("order_by", "t.published_at");
                params.put("cached", "1");
                params.put("android_id", android_id);
                buildLatestResultList(params);
            } else {
                Toast.makeText(context, getResources().getString(R.string.message_offline_simple), Toast.LENGTH_LONG);
                dummyData();
            }
        } catch (Exception e){e.printStackTrace();}
    }

    public void buildLatestResultList(final Map<String, String> params) {
        progress_bar.setVisibility(View.VISIBLE);
        ViewAnimation.collapse(lyt_suggestion);
        lyt_no_result.setVisibility(View.GONE);

        String url = Server.URL + "artist/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new SearchFragment.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result data : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                if (success == 1) {
                                    list_artists.clear();
                                    JSONObject data = jObj.getJSONObject("data");
                                    Iterator<String> keys = data.keys();

                                    while(keys.hasNext()) {
                                        String key = keys.next();
                                        SerializableArtist serializableArtist = new SerializableArtist(key, data.getString(key));
                                        list_artists.add(serializableArtist);
                                    }

                                    result_container.setVisibility(View.VISIBLE);
                                    lyt_no_result.setVisibility(View.GONE);

                                    rebuildList(list_artists);

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
                            dummyData();
                        }
                    }
                });
    }

    public String loadDummyArtists(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("artists.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    private void rebuildList(final List<SerializableArtist> _list_artists) {
        if (_list_artists.size() > 0) {
            adapterListArtist = new AdapterListArtist(context, _list_artists);
            recyclerResult.setAdapter(adapterListArtist);

            adapterListArtist.setOnItemClickListener(new AdapterListArtist.OnItemClickListener() {
                @Override
                public void onItemClick(View view, SerializableArtist obj, int position) {
                    Intent newActivity = new Intent(getActivity().getBaseContext(), ArtistDetailActivity.class);
                    SerializableArtist artist = obj;
                    newActivity.putExtra("artist_intent", artist);
                    startActivity(newActivity);
                }
            });
        }
    }
}
