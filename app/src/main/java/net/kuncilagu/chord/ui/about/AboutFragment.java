package net.kuncilagu.chord.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.kuncilagu.chord.R;
import net.kuncilagu.chord.utils.Server;

public class AboutFragment extends Fragment {

    private AboutViewModel aboutViewModel;
    private View root;
    TextView text_version;
    TextView website_version;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        aboutViewModel =
                ViewModelProviders.of(this).get(AboutViewModel.class);
        root = inflater.inflate(R.layout.fragment_about, container, false);

        initView();
        initAction();

        return root;
    }

    private void initView() {
        text_version = root.findViewById(R.id.text_version);
        website_version = root.findViewById(R.id.website_version);
    }

    private void initAction() {
        text_version.setText("Version : "+ getResources().getString(R.string.app_version));

        website_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Server.BASE_API_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
