package net.kuncilagu.chord.ui.request;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.kuncilagu.chord.MainActivity;
import net.kuncilagu.chord.R;
import net.kuncilagu.chord.utils.AppController;
import net.kuncilagu.chord.utils.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestChordsFragment extends Fragment {

    ProgressDialog pDialog;
    int success;

    private static final String TAG = RequestChordsFragment.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private RequestChordsViewModel requestChordViewModel;
    private View root;
    private EditText request_name;
    private EditText request_email;
    private EditText request_song_title;
    private EditText request_artist_name;
    private RadioGroup request_type;
    private TextView error_message;
    private TextView success_message;
    private Button btn_submit;
    private LinearLayout form_container;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        requestChordViewModel =
                ViewModelProviders.of(this).get(RequestChordsViewModel.class);
        root = inflater.inflate(R.layout.fragment_request_chords, container, false);

        initView();
        initAction();

        return root;
    }

    private void initView() {
        request_name = (EditText) root.findViewById(R.id.request_name);
        request_email = (EditText) root.findViewById(R.id.request_email);
        request_song_title = (EditText) root.findViewById(R.id.request_song_title);
        request_artist_name = (EditText) root.findViewById(R.id.request_artist_name);
        request_type = (RadioGroup) root.findViewById(R.id.request_type);
        error_message = (TextView) root.findViewById(R.id.error_message);
        success_message = (TextView) root.findViewById(R.id.success_message);
        btn_submit = (Button) root.findViewById(R.id.btn_submit);
        form_container = (LinearLayout) root.findViewById(R.id.form_container);
    }

    private void initAction() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _name = request_name.getText().toString();
                String _email = request_email.getText().toString();
                String _song_title = request_song_title.getText().toString();
                String _song_artist_name = request_artist_name.getText().toString();
                Integer _type = request_type.getCheckedRadioButtonId();
                Map<String, String> _data = new HashMap<String, String>();
                String errors = "";
                if (_name.length() <= 0) {
                    if (errors.length() > 0) {
                        errors += ", ";
                    }
                    errors += request_name.getHint().toString() +" "+ getResources().getString(R.string.error_empty_field);
                } else {
                    _data.put("name", _name);
                }

                if (_email.length() <= 0) {
                    if (errors.length() > 0) {
                        errors += ", ";
                    }
                    errors += request_email.getHint().toString() +" "+ getResources().getString(R.string.error_empty_field);
                } else {
                    if (!isEmailValid(_email)) {
                        if (errors.length() > 0) {
                            errors += ", ";
                        }
                        errors += getResources().getString(R.string.error_invalid_email);
                    }
                    _data.put("email", _email);
                }

                if (_song_title.length() <= 0) {
                    if (errors.length() > 0) {
                        errors += ", ";
                    }
                    errors += request_song_title.getHint().toString() +" "+ getResources().getString(R.string.error_empty_field);
                } else {
                    _data.put("title", _song_title);
                }

                if (_song_artist_name.length() <= 0) {
                    if (errors.length() > 0) {
                        errors += ", ";
                    }
                    errors += request_artist_name.getHint().toString() +" "+ getResources().getString(R.string.error_empty_field);
                } else {
                    _data.put("artist", _song_artist_name);
                }

                if (errors.length() > 0) {
                    error_message.setText(errors);
                    error_message.setVisibility(View.VISIBLE);
                    return;
                } else {
                    try {
                        doSubmitRequest(_data);
                    } catch (Exception e){e.printStackTrace();}
                }
            }
        });
    }

    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void doSubmitRequest(Map<String, String> params) {
        String url = Server.URL + "chord/request?api-key=" + Server.API_KEY;
        _string_request(Request.Method.POST, url, params, true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result data : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt(TAG_SUCCESS);
                                // Check for error node in json
                                if (success == 1) {
                                    error_message.setVisibility(View.GONE);
                                    success_message.setText(jObj.getString(TAG_MESSAGE));
                                    success_message.setVisibility(View.VISIBLE);
                                    form_container.setVisibility(View.GONE);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent newActivity = new Intent(getActivity().getBaseContext(), MainActivity.class);
                                            startActivity(newActivity);
                                        }
                                    }, 3000);
                                } else {
                                    success_message.setVisibility(View.GONE);
                                    error_message.setVisibility(View.VISIBLE);
                                    error_message.setText(jObj.getString(TAG_MESSAGE));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hideDialog();
                    }
                });
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(getContext());
            pDialog.setCancelable(false);
            pDialog.setMessage("Processing your request ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
            }
        }) {
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
