package com.adriankhor.spotifyview;

import android.support.v4.app.Fragment;

/**
 * Created by adriank09 on 05/03/2017.
 */

public class ListenFeedActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return ListenFeedFragment.newInstance();
    }
}
