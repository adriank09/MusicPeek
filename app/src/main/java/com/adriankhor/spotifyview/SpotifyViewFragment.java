package com.adriankhor.spotifyview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

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
        @Override
        protected List<SpotifyTrack> doInBackground(Void... params) {
            return new SpotifyAlbumFetcher().fetchTracks();
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> tracks) {
            mSpotifyTracks = tracks;
            setupAdapter();
        }
    }

    /***** private class for RecyclerView *****/

    // SpotifyViewHolder - to hold data
    private class SpotifyViewHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public SpotifyViewHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_spotify_track_image);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
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

            // places a placeholder image while thumbnail is being asynchronously
            // downloaded from Spotify
            Drawable placeholder = getResources().getDrawable(R.drawable.no_image);
            holder.bindDrawable(placeholder);

            // subsequently queues the thumbnail
            mThumbnailDownloader.queueThumbnail(holder, spotifyTrack.getTrackPreviewImage().toString());
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
}
