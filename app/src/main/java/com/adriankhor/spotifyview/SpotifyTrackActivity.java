package com.adriankhor.spotifyview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyTrackActivity extends SingleFragmentActivity {

    public static Intent newIntent (Context context, Uri trackUri) {
        Intent i = new Intent(context, SpotifyTrackActivity.class);
        i.setData(trackUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return SpotifyTrackFragment.newInstance(getIntent().getData());
    }
}
