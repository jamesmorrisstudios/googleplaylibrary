package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardItem;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardContainer extends BaseRecycleContainer {
    private final LeaderboardItem item;
    private final LeaderboardHeader header;

    public LeaderboardContainer(@NonNull LeaderboardHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    public LeaderboardContainer(@NonNull LeaderboardItem item) {
        super(false);
        this.header = null;
        this.item = item;
    }

    @Override
    public BaseRecycleItem getHeader() {
        return header;
    }

    @NonNull
    @Override
    public BaseRecycleItem getItem() {
        return item;
    }

}
