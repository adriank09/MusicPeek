package com.adriankhor.spotifyview;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adriankhor.spotifyview.database.ListenFeedDbAction;
import com.adriankhor.spotifyview.helper.QueryPreferences;
import com.adriankhor.spotifyview.model.ListenFeed;

import java.util.Collections;
import java.util.Comparator;
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
        setHasOptionsMenu(true);
        setActivityTitle(getString(R.string.listen_feed_activity_title));

        // fetch all feed entries from DB async
        new FetchFeedTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_spotify_listen_feed, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_listen_feed_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_listen_feed, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_feed_clear:
                clearAllFeedEntries();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***** private class *****/
    // holder for the RecyclerView for each feed item
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

        // binds data
        public void bindListenFeed(ListenFeed feed) {
            mTrackName.setText(feed.getName());
            mArtistName.setText(feed.getArtistName());
            mListenDate.setText(feed.getListenDate().toString());
        }

    }

    // adapter for the ListenFeed RecyclerView
    private class ListenFeedAdapter extends RecyclerView.Adapter<ListenFeedHolder> {
        private ListenFeedDbAction db = ListenFeedDbAction.newInstance(getActivity());

        // inflates the feed item into view and prep it for RecyclerView
        @Override
        public ListenFeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.listen_feed_item, parent, false);

            return new ListenFeedHolder(v);
        }

        // binds the feed data into the holder
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
    //

    // sets the activity title for this activity
    private void setActivityTitle(String text) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(text);
    }

    /*** private class ***/
    // fetches all feed entries from database
    private class FetchFeedTask extends AsyncTask<Void, Void, List<ListenFeed>> {
        @Override
        protected List<ListenFeed> doInBackground(Void... params) {
            ListenFeedDbAction db = ListenFeedDbAction.newInstance(getActivity());
            List<ListenFeed> lists = db.getAllFeedEntries();

            // reverse the order of all feeds - entries should display from latest to oldest
            Collections.reverse(lists);
            return lists;
        }

        @Override
        protected void onPostExecute(List<ListenFeed> listenFeeds) {
            // sets the retrieved feeds into the main List<ListenFeed> list
            mListenFeeds = listenFeeds;
            setupAdapter();

        }
    }

    // sets up the adapter for recyclerview
    private void setupAdapter() {
        if(isAdded()) {
            mRecyclerView.setAdapter(new ListenFeedAdapter());
        }
    }

    // clear all feed entries
    private void clearAllFeedEntries() {
        ListenFeedDbAction db = ListenFeedDbAction.newInstance(getContext());
        db.deleteAllFeedEntries();

        Toast.makeText(getContext(), "Listen feed entries cleared", Toast.LENGTH_SHORT).show();
    }

    private void updateItems() {
        new FetchFeedTask().execute();
    }
}
