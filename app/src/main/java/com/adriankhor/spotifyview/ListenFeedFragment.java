package com.adriankhor.spotifyview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adriankhor.spotifyview.database.ListenFeedDbAction;
import com.adriankhor.spotifyview.model.ListenFeed;

import java.util.List;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class ListenFeedFragment extends Fragment {
    private static final String TAG = "ListenFeedFragment";

    private RecyclerView mRecyclerView;
    private List<ListenFeed> mListenFeeds;

    public static ListenFeedFragment newInstance() {
        return new ListenFeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchFeedTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_spotify_listen_feed, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_listen_feed_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setActivityTitle(getString(R.string.listen_feed_activity_title));



        return v;
    }

    /***** private class *****/
    private class ListenFeedHolder extends RecyclerView.ViewHolder {

        private TextView mTrackName;
        private TextView mArtistName;
        private TextView mListenDate;

        public ListenFeedHolder(View itemView) {
            super(itemView);

            mTrackName = (TextView) itemView.findViewById(R.id.spotify_feed_item_track_name);
            mArtistName = (TextView) itemView.findViewById(R.id.spotify_feed_item_artist_name);
            mListenDate = (TextView) itemView.findViewById(R.id.spotify_feed_item_listen_date);

        }

        public void bindListenFeed(ListenFeed feed) {
            mTrackName.setText(feed.getName());
            mArtistName.setText(feed.getArtistName());
            mListenDate.setText(feed.getListenDate().toString());
        }

    }

    private class ListenFeedAdapter extends RecyclerView.Adapter<ListenFeedHolder> {

        private ListenFeedDbAction db = ListenFeedDbAction.newInstance(getActivity());

        public ListenFeedAdapter() {
            mListenFeeds = db.getAllFeedEntries();
        }

        @Override
        public ListenFeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.listen_feed_item, parent, false);

            return new ListenFeedHolder(v);
        }

        @Override
        public void onBindViewHolder(ListenFeedHolder holder, int position) {
            ListenFeed feed = mListenFeeds.get(position);
            holder.bindListenFeed(feed);

        }

        @Override
        public int getItemCount() {
            return mListenFeeds.size();
        }
    }

    // helper methods
    private void setActivityTitle(String text) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(text);
    }

    /*** private class ***/
    private class FetchFeedTask extends AsyncTask<Void, Void, List<ListenFeed>> {
        @Override
        protected List<ListenFeed> doInBackground(Void... params) {
            ListenFeedDbAction db = ListenFeedDbAction.newInstance(getActivity());

            return db.getAllFeedEntries();
        }

        @Override
        protected void onPostExecute(List<ListenFeed> listenFeeds) {
            mListenFeeds = listenFeeds;
            setupAdapter();

        }
    }

    private void setupAdapter() {
        if(isAdded()) {
            mRecyclerView.setAdapter(new ListenFeedAdapter());
        }
    }
}
