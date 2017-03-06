package com.adriankhor.spotifyview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.adriankhor.spotifyview.helper.QueryPreferences;
import com.adriankhor.spotifyview.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adriank09 on 05/03/2017.
 */

// deprecated

public class SpotifyTrackPagerActivity extends FragmentActivity {
    private static final String TAG = "SpotifyTrackPagerAct";
    private static final String EXTRA_SPOTIFY_TRACK_ID = "com.adriankhor.spotifyview.spotify_track_id";

    private ViewPager mViewPager;
    private static List<SpotifyTrack> mSpotifyTracks;

    public static Intent newIntent(Context packageContext, Uri trackID) {
        Intent i = new Intent(packageContext, SpotifyTrackPagerActivity.class);
        i.putExtra(EXTRA_SPOTIFY_TRACK_ID, trackID);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_track_pager);

        mViewPager = (ViewPager) findViewById(R.id.activity_spotify_track_view_pager);

        Uri uri = (Uri) getIntent().getSerializableExtra(EXTRA_SPOTIFY_TRACK_ID);

        mSpotifyTracks = new ArrayList<>();
        // find by fetching any recent query
        new FetchItemsTask(QueryPreferences.getStoredQuery(getApplicationContext())).execute();

        // at this point, mSpotifyTracks SHOULD have something
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                SpotifyTrack track = mSpotifyTracks.get(position);
                return SpotifyTrackFragment.newInstance(Uri.parse(track.getId()));
            }

            @Override
            public int getCount() {
                return mSpotifyTracks.size();
            }
        });

        // setting initial pager item
        for(int i=0;i<mSpotifyTracks.size();i++) {
            if(mSpotifyTracks.get(i).getId().equals(uri.toString())) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<SpotifyTrack>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
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
            mSpotifyTracks = tracks;
        }
    }
}
