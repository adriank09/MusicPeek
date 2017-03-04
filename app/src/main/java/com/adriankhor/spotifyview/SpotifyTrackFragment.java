package com.adriankhor.spotifyview;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyTrackFragment extends Fragment {
    private static final String TAG = "SpotifyTrackFragment";

    private static final String ARG_ARTIST_NAME = "spotify_track_artist_name";
    private static final String ARG_TRACK_NAME = "spotify_track_name";
    private static final String ARG_URI = "spotify_track_url";
    private static final String ARG_IMAGE = "spotify_track_image";

    private TextView mArtistName;
    private TextView mTrackName;
    private ImageView mTrackImage;
    private Uri mTrackUri;

    private static SpotifyTrack mSpotifyTrack;
    private static MediaPlayer mMediaPlayer;
    private static ProgressDialog mProgressDialog;

    public static SpotifyTrackFragment newInstance(Uri trackUri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, trackUri);

        SpotifyTrackFragment fragment = new SpotifyTrackFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackUri = getArguments().getParcelable(ARG_URI);

        new DownloadImage().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_spotify_track_playing, container, false);

        mArtistName = (TextView) v.findViewById(R.id.spotify_track_playing_artist_name);
        mTrackImage = (ImageView) v.findViewById(R.id.spotify_track_playing_image);
        mTrackName = (TextView) v.findViewById(R.id.spotify_track_playing_title);

        return v;
    }

    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<Void, Void, Bitmap> {

        private SpotifyTrack mSpotifyTrack;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("Loading track");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);

            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            mSpotifyTrack = new SpotifyAlbumFetcher().getTrack(mTrackUri);
            String imageURL = mSpotifyTrack.getTrackPreviewImage().toString();

            Bitmap bitmap = null;

            try {
                InputStream input = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            // Set the bitmap into ImageView
            mTrackImage.setImageBitmap(result);

            mTrackName.setText(mSpotifyTrack.getName());
            mArtistName.setText(mSpotifyTrack.getArtistName());

            playTrack();

            mProgressDialog.dismiss();
        }

        // plays the preview track
        private void playTrack() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mMediaPlayer.setDataSource(mSpotifyTrack.getPreviewUri().toString());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
            catch (IOException ioe) {
                Log.e(TAG, "Unable to play track", ioe);
            }
        }
    }

    // MediaPlayer AsyncTask

}
