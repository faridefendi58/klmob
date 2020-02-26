package net.kuncilagu.chord.ui.artist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.kuncilagu.chord.R;

public class AdapterListArtist extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<SerializableArtist> items = new ArrayList<>();
    private List<SerializableArtist> artistListFiltered = new ArrayList<>();

    private Context ctx;

    private AdapterListArtist.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, SerializableArtist obj, int position);
    }

    public void setOnItemClickListener(final AdapterListArtist.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListArtist(Context context, List<SerializableArtist> items) {
        this.ctx = context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView artist_name;
        public TextView song_title;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            artist_name = (TextView) v.findViewById(R.id.artist_name);
            song_title = (TextView) v.findViewById(R.id.song_title);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        vh = new AdapterListArtist.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListArtist.OriginalViewHolder) {
            final AdapterListArtist.OriginalViewHolder view = (AdapterListArtist.OriginalViewHolder) holder;
            AdapterListArtist.OriginalViewHolder vwh = (AdapterListArtist.OriginalViewHolder) holder;

            try {
                SerializableArtist artist = items.get(position);
                Map<String, String> artist_map = artist.toMap();
                view.artist_name.setText(artist_map.get("name"));
            } catch (Exception e){}

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    artistListFiltered = items;
                } else {
                    List<SerializableArtist> filteredList = new ArrayList<>();
                    for (SerializableArtist row : items) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    artistListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = artistListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                artistListFiltered = (ArrayList<SerializableArtist>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
