package com.adriankhor.spotifyview;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private static final String ARG_ARTIST_NAME = "spotify_track_artist_name";
    private static final String ARG_TRACK_NAME = "spotify_track_name";
    private static final String ARG_URI = "spotify_track_url";
    private static final String ARG_IMAGE = "spotify_track_image";

    private TextView mArtistName;
    private TextView mTrackName;
    private ImageView mTrackImage;
    private Uri mTrackUri;

    private SpotifyTrack mSpotifyTrack;

    private ProgressDialog mProgressDialog;

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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(getActivity());
            // Set progressdialog title
            mProgressDialog.setTitle("Loading image");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            SpotifyTrack track = new SpotifyAlbumFetcher().getTrack(mTrackUri);
            String imageURL = track.getTrackPreviewImage().toString();

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
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
            // Close progressdialog
            mProgressDialog.dismiss();
        }
    }

}
