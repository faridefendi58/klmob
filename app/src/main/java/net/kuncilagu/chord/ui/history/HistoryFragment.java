package net.kuncilagu.chord.ui.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import net.kuncilagu.chord.R;
import net.kuncilagu.chord.controllers.HistoryController;
import net.kuncilagu.chord.ui.search.SearchDetailActivity;
import net.kuncilagu.chord.ui.search.SerializableChord;
import net.kuncilagu.chord.utils.ViewAnimation;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private View root;
    private Context context;

    private ProgressBar progress_bar;
    private LinearLayout lyt_no_result;

    private LinearLayout result_container;
    private RecyclerView recyclerResult;
    private List<SerializableChord> list_chords = new ArrayList<SerializableChord>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
        root = inflater.inflate(R.layout.fragment_history, container, false);
        context = container.getContext();

        initComponent();
        buildSearchResultList();
        return root;
    }

    private void initComponent() {
        progress_bar = (ProgressBar) root.findViewById(R.id.progress_bar);
        lyt_no_result = (LinearLayout) root.findViewById(R.id.lyt_no_result);

        result_container = (LinearLayout) root.findViewById(R.id.result_container);
        recyclerResult = (RecyclerView) root.findViewById(R.id.recyclerResult);
        recyclerResult.setLayoutManager(new LinearLayoutManager(context));
        recyclerResult.setHasFixedSize(true);
    }

    private void buildSearchResultList() {
        try {
            progress_bar.setVisibility(View.VISIBLE);
            lyt_no_result.setVisibility(View.GONE);

            list_chords = HistoryController.getInstance().getSongs(100);
        } catch (Exception e){e.printStackTrace();}

        if (list_chords.size() > 0) {
            result_container.setVisibility(View.VISIBLE);
            lyt_no_result.setVisibility(View.GONE);

            AdapterListHistory sAdapter = new AdapterListHistory(context, list_chords);
            recyclerResult.setAdapter(sAdapter);

            sAdapter.setOnItemClickListener(new AdapterListHistory.OnItemClickListener() {
                @Override
                public void onItemClick(View view, SerializableChord obj, int position) {
                    Intent newActivity = new Intent(getActivity().getBaseContext(), SearchDetailActivity.class);
                    SerializableChord chord = list_chords.get(position);
                    newActivity.putExtra("chord_intent", chord);
                    startActivity(newActivity);
                }
            });

            lyt_no_result.setVisibility(View.GONE);
        } else {
            lyt_no_result.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        }, 1000);
    }
}

