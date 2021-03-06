package com.adriankhor.spotifyview;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adriankhor.spotifyview.database.ListenFeedDbAction;
import com.adriankhor.spotifyview.database.SpotifyViewDbSchema;
import com.adriankhor.spotifyview.database.SpotifyViewDbSchema.SpotifyViewFeedTable;
import com.adriankhor.spotifyview.model.ListenFeed;
import com.adriankhor.spotifyview.model.SpotifyTrack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyTrackFragment extends Fragment {
    private static final String TAG = "SpotifyTrackFragment";

    private static final String ARG_URI = "spotify_track_url";

    private static final String TRACK_POSITION = "spotify_track_position";

    private static final int NOW_PLAYING_ID = 1;

    private TextView mArtistName;
    private TextView mTrackName;
    private ImageView mTrackImage;
    private Uri mTrackUri;

    private ImageButton mPauseButton;
    private Button mShareButton;

    private static SpotifyTrack mSpotifyTrack;
    private static MediaPlayer mMediaPlayer;
    private static ProgressDialog mProgressDialog;

    protected static String mPreviewURL;

    // creates a new instance with bundle containing track Uri
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

        mMediaPlayer = new MediaPlayer();
        mTrackUri = getArguments().getParcelable(ARG_URI);

        new DownloadImage().execute();
    }

    private void addIntoFeed(SpotifyTrack track) {
        ListenFeedDbAction db = ListenFeedDbAction.newInstance(getActivity());
        ListenFeed feed = new ListenFeed();
        feed.setId(track.getId());
        feed.setName(track.getName());
        feed.setArtistName(track.getArtistName());

        db.addListenFeedEntry(feed);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    // save current track playing position before changes in orientation is done
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TRACK_POSITION, mMediaPlayer.getCurrentPosition());
        Log.i(TAG, "Position now before rotate:"+mMediaPlayer.getCurrentPosition());
        //mMediaPlayer.pause();

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            int position = savedInstanceState.getInt(TRACK_POSITION);
            Log.i(TAG, "Position now after rotate:"+position);

            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            int position = savedInstanceState.getInt(TRACK_POSITION);
            mMediaPlayer.seekTo(position);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_spotify_track_playing, container, false);

        mArtistName = (TextView) v.findViewById(R.id.spotify_track_playing_artist_name);
        mTrackImage = (ImageView) v.findViewById(R.id.spotify_track_playing_image);
        mTrackName = (TextView) v.findViewById(R.id.spotify_track_playing_title);

        mPauseButton = (ImageButton) v.findViewById(R.id.spotify_track_playing_button_pause);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMediaPlayer.isPlaying()) {
                    mPauseButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mMediaPlayer.pause();
                } else {
                    mPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mMediaPlayer.start();
                }
            }
        });

        mShareButton = (Button) v.findViewById(R.id.spotify_track_playing_share);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // message title
                String title = "Listen to this song!";

                // message content
                String text = getString(R.string.message_currently_listening_to,
                        mTrackName.getText(),
                        mArtistName.getText(),
                        mPreviewURL);

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, text);
                i.putExtra(Intent.EXTRA_TITLE, title);

                // allow content to be sent by other apps
                i = Intent.createChooser(i, "Share to others via");
                startActivity(i);

            }
        });
        return v;
    }

    // changes the activity title - based on the track name
    // ref: http://stackoverflow.com/questions/28954445/set-toolbar-title
    private void setActivityTitle(String text) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(text);
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
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);

            if(mSpotifyTrack.getTrackPreviewImage() != null) {
                String imageURL = mSpotifyTrack.getTrackPreviewImage().toString();

                try {
                    InputStream input = new java.net.URL(imageURL).openStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            // Set the bitmap into ImageView
            mTrackImage.setImageBitmap(result);

            mTrackName.setText(mSpotifyTrack.getName());
            mArtistName.setText(mSpotifyTrack.getArtistName());

            setActivityTitle(mSpotifyTrack.getName());

            bootstrapPlayer();

            addIntoFeed(mSpotifyTrack);

            mPreviewURL = mSpotifyTrack.getPreviewUri().toString();
            showNowPlayingNotification(mSpotifyTrack);
            mProgressDialog.dismiss();
        }

        // prepares the player to be played
        private void bootstrapPlayer() {

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPauseButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            });
            // plays the track
            playSpotifyTrack(mSpotifyTrack.getPreviewUri().toString());
        }

        // plays the preview track
        private void playSpotifyTrack(String url) {
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
            catch (IOException ioe) {
                Log.e(TAG, "Unable to play track", ioe);
            }
        }

        // shows the notification
        private void showNowPlayingNotification(SpotifyTrack track) {

            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                    new NotificationCompat.Builder(getContext())
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentTitle("Now Playing")
                    .setContentText(track.getName() + " by " + track.getArtistName());

            // content intent should be a new, empty one - when tapped in Notification Center, nothing happens (intended)
            mBuilder.setContentIntent(PendingIntent.getActivity(getContext(), 0, new Intent(), 0));
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(NOW_PLAYING_ID, mBuilder.build());
        }
    }
}
