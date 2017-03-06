package com.adriankhor.spotifyview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adriankhor.spotifyview.helper.QueryPreferences;
import com.adriankhor.spotifyview.helper.ThumbnailDownloader;
import com.adriankhor.spotifyview.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyViewFragment extends Fragment {
    private static final String TAG = "SpotifyViewFragment";

    private RecyclerView mSpotifyViewRecyclerView;
    private List<SpotifyTrack> mSpotifyTracks = new ArrayList<>();
    private ThumbnailDownloader<SpotifyViewHolder> mThumbnailDownloader;

    private static ProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        Handler responseHandler = new Handler();

        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<SpotifyViewHolder>() {
            @Override
            public void onThumbnailDownloaded(SpotifyViewHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_spotify_track, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        // listens to changes made on query text
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // acts upon the query text
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_clear:
                // clears the stored query
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_view_feed:
                Intent i = new Intent(getContext(), ListenFeedActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    // creating view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_layout, container, false);

        mSpotifyViewRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_main_recycler_view);
        mSpotifyViewRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        return v;
    }

    /***** private class for Async FetchItemTask *****/
    private class FetchItemsTask extends AsyncTask<Void,Void,List<SpotifyTrack>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("Loading tracks");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);

            mProgressDialog.show();
        }

        @Override
        protected List<SpotifyTrack> doInBackground(Void... params) {
            List<SpotifyTrack> tracks = new ArrayList<>();
            if(mQuery == null) {
                tracks = new SpotifyAlbumFetcher().fetchTracks();
            } else {
                tracks = new SpotifyAlbumFetcher().searchTracks(mQuery);
            }

            return tracks;
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> tracks) {
            Log.i(TAG, "onPostExecute entered");
            mSpotifyTracks = tracks;

            setupAdapter();
            mProgressDialog.dismiss();
            Log.i(TAG, "onPostExecute end");
        }
    }

    /***** private class for RecyclerView *****/
    // SpotifyViewHolder - to hold data
    private class SpotifyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private SpotifyTrack mSpotifyTrack;

        public SpotifyViewHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_spotify_track_image);
            mItemImageView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindSpotifyTrack(SpotifyTrack track) {
            mSpotifyTrack = track;
        }

        @Override
        public void onClick(View v) {
            Intent intent = SpotifyTrackActivity.newIntent(getActivity(), Uri.parse(mSpotifyTrack.getId()));
            startActivity(intent);
        }
    }

    // SpotifyViewAdapter - to plug data into RecyclerView
    private class SpotifyViewAdapter extends RecyclerView.Adapter<SpotifyViewHolder> {
        private List<SpotifyTrack> mSpotifyTracks;

        public SpotifyViewAdapter(List<SpotifyTrack> spotifyTracks) {
            mSpotifyTracks = spotifyTracks;
        }

        @Override
        public SpotifyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.spotify_track, parent, false);
            return new SpotifyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SpotifyViewHolder holder, int position) {
            SpotifyTrack spotifyTrack = mSpotifyTracks.get(position);
            holder.bindSpotifyTrack(spotifyTrack);

            // places a placeholder image while thumbnail is being asynchronously
            // downloaded from Spotify
            Drawable placeholder = getResources().getDrawable(R.drawable.no_image);
            holder.bindDrawable(placeholder);

            // subsequently queues the thumbnail
            mThumbnailDownloader.queueThumbnail(holder, spotifyTrack.getTrackPreviewImage());
        }

        @Override
        public int getItemCount() {
            return mSpotifyTracks.size();
        }
    }

    /***** helper methods *****/
    public static SpotifyViewFragment newInstance() {
        return new SpotifyViewFragment();
    }

    private void setupAdapter() {
        if(isAdded()) {
            mSpotifyViewRecyclerView.setAdapter(new SpotifyViewAdapter(mSpotifyTracks));

        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }
}
