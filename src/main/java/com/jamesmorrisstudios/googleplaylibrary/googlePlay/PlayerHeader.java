package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerHeader extends BaseRecycleItem {
    public final String title;

    public PlayerHeader(@NonNull String title) {
        this.title = title;
    }
}

