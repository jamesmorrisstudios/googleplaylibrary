package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaItem;

/**
 * Leaderboard Meta data container.
 *
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaContainer extends BaseRecycleContainer {
    private final LeaderboardMetaItem item;

    /**
     * Constructor for item
     * @param item Item data
     */
    public LeaderboardMetaContainer(@NonNull LeaderboardMetaItem item) {
        super(false);
        this.item = item;
    }

    /**
     * If using the item this cannot be null
     * @return The item data
     */
    @NonNull
    @Override
    public BaseRecycleItem getItem() {
        return item;
    }

}
