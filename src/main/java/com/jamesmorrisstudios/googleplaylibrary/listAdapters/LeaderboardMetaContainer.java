package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardMetaItem;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaContainer extends BaseRecycleContainer {
    private final LeaderboardMetaItem item;

    public LeaderboardMetaContainer(@NonNull LeaderboardMetaItem item) {
        super(false);
        this.item = item;
    }

    @Override
    public BaseRecycleItem getHeader() {
        return null;
    }

    @NonNull
    @Override
    public BaseRecycleItem getItem() {
        return item;
    }

}
