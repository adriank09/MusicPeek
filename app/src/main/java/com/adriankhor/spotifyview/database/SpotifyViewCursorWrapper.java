package com.adriankhor.spotifyview.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.adriankhor.spotifyview.database.SpotifyViewDbSchema.SpotifyViewFeedTable;
import com.adriankhor.spotifyview.model.ListenFeed;
import com.adriankhor.spotifyview.model.SpotifyTrack;

import java.util.Date;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class SpotifyViewCursorWrapper extends CursorWrapper {
    public SpotifyViewCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ListenFeed getListenFeed() {
        String id = getString(getColumnIndex(SpotifyViewFeedTable.Cols.UUID));
        String artist_name = getString(getColumnIndex(SpotifyViewFeedTable.Cols.ARTIST_NAME));
        String track_name = getString(getColumnIndex(SpotifyViewFeedTable.Cols.TRACK_NAME));
        long listen_date = getLong(getColumnIndex(SpotifyViewFeedTable.Cols.DATE));

        ListenFeed feed = new ListenFeed();
        feed.setId(id);
        feed.setName(track_name);
        feed.setArtistName(artist_name);
        feed.setListenDate(new Date(listen_date));

        return feed;
    }
}
