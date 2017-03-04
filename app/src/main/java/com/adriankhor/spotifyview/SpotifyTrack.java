package com.adriankhor.spotifyview;

import android.net.Uri;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyTrack {
    private String mId;
    private String mName;
    private Uri mPreviewMP3Uri;
    private Uri mTrackPreviewImage;

    public Uri getTrackPreviewImage() {
        return mTrackPreviewImage;
    }

    public void setTrackPreviewImage(Uri mTrackPreviewImage) {
        this.mTrackPreviewImage = mTrackPreviewImage;
    }



    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Uri getPreviewUri() {
        return mPreviewMP3Uri;
    }

    public void setPreviewUri(Uri mPreviewUri) {
        this.mPreviewMP3Uri = mPreviewUri;
    }
}
