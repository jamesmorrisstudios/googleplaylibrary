package com.jamesmorrisstudios.googleplaylibrary.data;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerHeader extends BaseRecycleItem {
    public final String title;

    public PlayerPickerHeader(@NonNull String title) {
        this.title = title;
    }
}

