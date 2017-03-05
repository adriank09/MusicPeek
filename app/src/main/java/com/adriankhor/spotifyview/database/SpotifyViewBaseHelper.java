package com.adriankhor.spotifyview.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.adriankhor.spotifyview.database.SpotifyViewDbSchema.SpotifyViewFeedTable;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class SpotifyViewBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "spotifyView.db";

    private static final String SQL_CREATE_TABLE =
            "create table " + SpotifyViewFeedTable.NAME + " (" +
                "_id integer primary key autoincrement, " +
                SpotifyViewFeedTable.Cols.UUID + ", " +
                SpotifyViewFeedTable.Cols.TRACK_NAME + ", " +
                SpotifyViewFeedTable.Cols.ARTIST_NAME + ", " +
                SpotifyViewFeedTable.Cols.DATE +
            ")";

    public SpotifyViewBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
