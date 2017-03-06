package com.adriankhor.spotifyview.model;

import android.net.Uri;

import java.util.Date;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class ListenFeed {

    private String mId;
    private String mName;
    private String mArtistName;
    private Date mListenDate;

    public ListenFeed() {
        mListenDate = new Date();
    }

    public Date getListenDate() {
        return mListenDate;
    }

    public void setListenDate(Date mListenDate) {
        this.mListenDate = mListenDate;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
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

}
