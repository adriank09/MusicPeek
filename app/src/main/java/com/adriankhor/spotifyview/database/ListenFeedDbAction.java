package com.adriankhor.spotifyview.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.adriankhor.spotifyview.database.SpotifyViewDbSchema.SpotifyViewFeedTable;
import com.adriankhor.spotifyview.model.ListenFeed;
import com.adriankhor.spotifyview.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class ListenFeedDbAction {

    private SQLiteDatabase mDatabase;
    private static ListenFeedDbAction _instance;

    // Singleton constructor
    public static ListenFeedDbAction newInstance(Context context) {
        if(_instance == null) {
            return new ListenFeedDbAction(context);
        }
        return _instance;
    }

    // adds an entry to the database
    public void addListenFeedEntry(ListenFeed feed) {
        ContentValues cv = getContentValues(feed);

        mDatabase.insert(SpotifyViewFeedTable.NAME, null, cv);
    }

    // getting list of tracks stored in database
    public List<ListenFeed> getAllFeedEntries() {
        List<ListenFeed> listenFeeds = new ArrayList<>();

        SpotifyViewCursorWrapper cursor = queryTracks(null,null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                listenFeeds.add(cursor.getListenFeed());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return listenFeeds;
    }

    public void deleteAllFeedEntries() {
        mDatabase.delete(SpotifyViewFeedTable.NAME, null, null);
    }

    /***** private methods *****/
    // private constructor
    private ListenFeedDbAction(Context context) {
        mDatabase = new SpotifyViewBaseHelper(context.getApplicationContext()).getWritableDatabase();
    }

    // ContentValues for writing into database
    private ContentValues getContentValues(ListenFeed feed) {
        ContentValues values = new ContentValues();

        values.put(SpotifyViewFeedTable.Cols.UUID, feed.getId());
        values.put(SpotifyViewFeedTable.Cols.TRACK_NAME, feed.getName());
        values.put(SpotifyViewFeedTable.Cols.ARTIST_NAME, feed.getArtistName());
        values.put(SpotifyViewFeedTable.Cols.DATE, new Date().getTime());

        return values;
    }

    // query tracks internally by using the custom cursorwrapper class
    private SpotifyViewCursorWrapper queryTracks(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                SpotifyViewFeedTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new SpotifyViewCursorWrapper(cursor);
    }
}
