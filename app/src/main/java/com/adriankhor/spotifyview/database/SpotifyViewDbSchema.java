package com.adriankhor.spotifyview.database;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class SpotifyViewDbSchema {
    public static final class SpotifyViewFeedTable {
        public static final String NAME = "SpotifyViewFeed";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TRACK_NAME = "track_name";
            public static final String ARTIST_NAME = "artist_name";
            public static final String DATE = "date";
        }
    }


}
